

package io.mycat.memory.unsafe.utils.sort;

import com.google.common.annotations.VisibleForTesting;

import io.mycat.MycatServer;
import io.mycat.memory.unsafe.Platform;
import io.mycat.memory.unsafe.array.LongArray;
import io.mycat.memory.unsafe.memory.MemoryBlock;
import io.mycat.memory.unsafe.memory.mm.DataNodeMemoryManager;
import io.mycat.memory.unsafe.memory.mm.MemoryConsumer;
import io.mycat.memory.unsafe.storage.DataNodeDiskManager;
import io.mycat.memory.unsafe.storage.SerializerManager;
import io.mycat.memory.unsafe.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * External sorter based on {@link UnsafeInMemorySorter}.
 */
public final class UnsafeExternalSorter extends MemoryConsumer {

  private final Logger logger = LoggerFactory.getLogger(UnsafeExternalSorter.class);

  @Nullable
  private final PrefixComparator prefixComparator;
  @Nullable
  private final RecordComparator recordComparator;


  private final DataNodeMemoryManager dataNodeMemoryManager;
  private final DataNodeDiskManager blockManager;
  private final SerializerManager serializerManager;


  /** The buffer size to use when writing spills using DiskRowWriter */
  private final int fileBufferSizeBytes;

  /**
   * Memory pages that hold the records being sorted. The pages in this list are freed when
   * spilling, although in principle we could recycle these pages across spills (on the other hand,
   * this might not be necessary if we maintained a pool of re-usable pages in the DataNodeMemoryManager
   * itself).
   */
  private final LinkedList<MemoryBlock> allocatedPages = new LinkedList<MemoryBlock>();

  private final LinkedList<UnsafeSorterSpillWriter> spillWriters = new LinkedList<UnsafeSorterSpillWriter>();

  // These variables are reset after spilling:
  @Nullable
  private volatile UnsafeInMemorySorter inMemSorter;

  private MemoryBlock currentPage = null;
  private long pageCursor = -1;
  private long peakMemoryUsedBytes = 0;
  private long totalSpillBytes = 0L;
  private long totalSortTimeNanos = 0L;
  private volatile SpillableIterator readingIterator = null;

  public static UnsafeExternalSorter createWithExistingInMemorySorter(
      DataNodeMemoryManager dataNodeMemoryManager,
      DataNodeDiskManager blockManager,
      SerializerManager serializerManager,
      RecordComparator recordComparator,
      PrefixComparator prefixComparator,
      int initialSize,
      long pageSizeBytes,
      UnsafeInMemorySorter inMemorySorter,boolean enableSort) throws IOException {

    UnsafeExternalSorter sorter = new UnsafeExternalSorter(dataNodeMemoryManager, blockManager,
      serializerManager,recordComparator, prefixComparator, initialSize,
        pageSizeBytes, inMemorySorter, false /* ignored */,enableSort);

    sorter.spill(Long.MAX_VALUE, sorter);
    // The external sorter will be used to insert records, in-memory sorter is not needed.
    sorter.inMemSorter = null;
    return sorter;
  }

  public static UnsafeExternalSorter create(
          DataNodeMemoryManager dataNodeMemoryManager,
          DataNodeDiskManager blockManager,
          SerializerManager serializerManager,
          RecordComparator recordComparator,
          PrefixComparator prefixComparator,
          long initialSize,
          long pageSizeBytes,
          boolean canUseRadixSort,
          boolean enableSort) {

    return new UnsafeExternalSorter(dataNodeMemoryManager, blockManager, serializerManager, recordComparator, prefixComparator, initialSize, pageSizeBytes, null,
      canUseRadixSort,enableSort);

  }

  private UnsafeExternalSorter(
      DataNodeMemoryManager dataNodeMemoryManager,
      DataNodeDiskManager blockManager,
      SerializerManager serializerManager,
      RecordComparator recordComparator,
      PrefixComparator prefixComparator,
      long initialSize,
      long pageSizeBytes,
      @Nullable UnsafeInMemorySorter existingInMemorySorter,
      boolean canUseRadixSort,boolean enableSort) {

    super(dataNodeMemoryManager, pageSizeBytes);

    this.dataNodeMemoryManager = dataNodeMemoryManager;
    this.blockManager = blockManager;
    this.serializerManager = serializerManager;
    this.recordComparator = recordComparator;
    this.prefixComparator = prefixComparator;


    if(MycatServer.getInstance().getMyCatMemory() != null){
         this.fileBufferSizeBytes = (int) MycatServer.getInstance().
              getMyCatMemory().getConf().getSizeAsBytes("mycat.merge.file.buffer", "32k");
    }else{
      this.fileBufferSizeBytes = 32*1024;
    }

    if (existingInMemorySorter == null) {
      this.inMemSorter = new UnsafeInMemorySorter(
        this, dataNodeMemoryManager, recordComparator, prefixComparator, initialSize, canUseRadixSort,enableSort);
    } else {
      this.inMemSorter = existingInMemorySorter;
    }

    this.peakMemoryUsedBytes = getMemoryUsage();
  }

  /**
   * Marks the current page as no-more-space-available, and as a result, either allocate a
   * new page or spill when we see the next record.
   */
  @VisibleForTesting
  public void closeCurrentPage() {
    if (currentPage != null) {
      pageCursor = currentPage.getBaseOffset() + currentPage.size();
    }
  }

  /**
   * Sort and spill the current records in response to memory pressure.
   */
  @Override
  public long spill(long size, MemoryConsumer trigger) throws IOException {
    if (trigger != this) {
      if (readingIterator != null) {
        return readingIterator.spill();
      }
      return 0L; // this should throw exception
    }

    if (inMemSorter == null || inMemSorter.numRecords() <= 0) {
      return 0L;
    }

    logger.info("Thread"  +   Thread.currentThread().getId() +" spilling sort data of "+  JavaUtils.bytesToString(getMemoryUsage())
            +" to disk ("+ spillWriters.size()+" times so far)");

    // We only write out contents of the inMemSorter if it is not empty.
    if (inMemSorter.numRecords() > 0) {

      /**
       * ????????????????????????SpillWriter????????????????????????????????????????????????????????????.
       */
      final UnsafeSorterSpillWriter spillWriter = new UnsafeSorterSpillWriter(blockManager, fileBufferSizeBytes,/**writeMetrics,*/ inMemSorter.numRecords());

       /**
       * ?????????SpillWriter?????????????????????????????? spillWriters.size()?????????????????????
       */
      spillWriters.add(spillWriter);

        /**
         * ????????????????????????????????????????????????????????????In Memory Sort use time sorter ?????? radix sorter
         */
      final UnsafeSorterIterator sortedRecords = inMemSorter.getSortedIterator();

       /**
       * ??????????????????????????????
       */
      while (sortedRecords.hasNext()) {
        /**
         *
         */
        sortedRecords.loadNext();
        /**
         * ??????????????????????????????????????????on-heap???obj???off-heap???null
         */
        final Object baseObject = sortedRecords.getBaseObject();

        /**
         * ????????????????????????????????????????????????????????????
         */
        final long baseOffset = sortedRecords.getBaseOffset();

        /**
         * ?????????????????????
         */
        final int recordLength = sortedRecords.getRecordLength();
        /**
         * ????????????????????????????????? Write a record to a spill file.
         */
        spillWriter.write(baseObject, baseOffset, recordLength, sortedRecords.getKeyPrefix());
      }

        /**
         * ??????spillWriter
         */
      spillWriter.close();
    }

    /**
     * ????????????sorter?????????????????????
     */
    final long spillSize = freeMemory();
    // Note that this is more-or-less going to be a multiple of the page size, so wasted space in
    // pages will currently be counted as memory spilled even though that space isn't actually
    // written to disk. This also counts the space needed to store the sorter's pointer array.
    inMemSorter.reset();
    // Reset the in-memory sorter's pointer array only after freeing up the memory pages holding the
    // records. Otherwise, if the task is over allocated memory, then without freeing the memory
    // pages, we might not be able to get memory for the pointer array.

    totalSpillBytes += spillSize;
    return spillSize;
  }

  /**
   * Return the total memory usage of this sorter, including the data pages and the sorter's pointer
   * array.
   */
  private long getMemoryUsage() {
    long totalPageSize = 0;
    for (MemoryBlock page : allocatedPages) {
      totalPageSize += page.size();
    }
    return ((inMemSorter == null) ? 0 : inMemSorter.getMemoryUsage()) + totalPageSize;
  }

  private void updatePeakMemoryUsed() {
    long mem = getMemoryUsage();
    if (mem > peakMemoryUsedBytes) {
      peakMemoryUsedBytes = mem;
    }
  }

  /**
   * Return the peak memory used so far, in bytes.
   */
  public long getPeakMemoryUsedBytes() {
    updatePeakMemoryUsed();
    return peakMemoryUsedBytes;
  }

  /**
   * @return the total amount of time spent sorting data (in-memory only).
   */
  public long getSortTimeNanos() {
    UnsafeInMemorySorter sorter = inMemSorter;
    if (sorter != null) {
      return sorter.getSortTimeNanos();
    }
    return totalSortTimeNanos;
  }

  /**
   * Return the total number of bytes that has been spilled into disk so far.
   */
  public long getSpillSize() {
    return totalSpillBytes;
  }

  @VisibleForTesting
  public int getNumberOfAllocatedPages() {
    return allocatedPages.size();
  }

  /**
   * Free this sorter's data pages.
   *
   * @return the number of bytes freed.
   */
  private long freeMemory() {
    updatePeakMemoryUsed();
    long memoryFreed = 0;
    for (MemoryBlock block : allocatedPages) {
      memoryFreed += block.size();
      freePage(block);
    }
    allocatedPages.clear();
    currentPage = null;
    pageCursor = 0;
    return memoryFreed;
  }

  /**
   * Deletes any spill files created by this sorter.
   */
  private void deleteSpillFiles() {
    for (UnsafeSorterSpillWriter spill : spillWriters) {
      File file = spill.getFile();
      if(file == null)
        continue;
      try {
        JavaUtils.deleteRecursively(file.getParentFile().getParentFile());
      } catch (IOException e) {
        logger.error(e.getMessage());
      }

      if (file.exists()) {
        if (!file.delete()) {
          logger.error("Was unable to delete spill file {}", file.getAbsolutePath());
        }
      }
    }
  }

  /**
   * Frees this sorter's in-memory data structures and cleans up its spill files.
   */
  public void cleanupResources() {
    synchronized (this) {
      deleteSpillFiles();
      freeMemory();
      if (inMemSorter != null) {
        inMemSorter.free();
        inMemSorter = null;
      }
    }
  }

  /**
   * Checks whether there is enough space to insert an additional record in to the sort pointer
   * array and grows the array if additional space is required. If the required space cannot be
   * obtained, then the in-memory data will be spilled to disk.
   */
  private void growPointerArrayIfNecessary() throws IOException {
    assert(inMemSorter != null);
    if (!inMemSorter.hasSpaceForAnotherRecord()) {
      long used = inMemSorter.getMemoryUsage();
      LongArray array;
      try {
        // could trigger spilling
        array = allocateLongArray(used / 8 * 2);
      } catch (OutOfMemoryError e) {
        // should have trigger spilling
        if (!inMemSorter.hasSpaceForAnotherRecord()) {
          logger.error("Unable to grow the pointer array");
          throw e;
        }
        return;
      }
      // check if spilling is triggered or not
      if (inMemSorter.hasSpaceForAnotherRecord()) {
        freeLongArray(array);
      } else {
        inMemSorter.expandPointerArray(array);
      }
    }
  }

  /**
   * Allocates more memory in order to insert an additional record. This will request additional
   * memory from the memory manager and spill if the requested memory can not be obtained.
   *
   * @param required the required space in the data page, in bytes, including space for storing
   *                      the record size. This must be less than or equal to the page size (records
   *                      that exceed the page size are handled via a different code path which uses
   *                      special overflow pages).
   */
  private void acquireNewPageIfNecessary(int required) {
    if (currentPage == null ||
      pageCursor + required > currentPage.getBaseOffset() + currentPage.size()) {
      // TODO: try to find space on previous pages
      currentPage = allocatePage(required);
      pageCursor = currentPage.getBaseOffset();
      allocatedPages.add(currentPage);
    }
  }

  /**
   * Write a record to the sorter.
   */
  public void insertRecord(Object recordBase, long recordOffset, int length, long prefix)
    throws IOException {

    growPointerArrayIfNecessary();
    // Need 4 bytes to store the record length.
    final int required = length + 4;
    acquireNewPageIfNecessary(required);

    final Object base = currentPage.getBaseObject();

    final long recordAddress = dataNodeMemoryManager.encodePageNumberAndOffset(currentPage,pageCursor);
    Platform.putInt(base, pageCursor, length);
    pageCursor += 4;
    Platform.copyMemory(recordBase,recordOffset,base,pageCursor,length);
    pageCursor += length;
    assert(inMemSorter != null);
    inMemSorter.insertRecord(recordAddress,prefix);
  }

  /**
   * Write a key-value record to the sorter. The key and value will be put together in-memory,
   * using the following format:
   *
   * record length (4 bytes), key length (4 bytes), key data, value data
   *
   * record length = key length + value length + 4
   */
  public void insertKVRecord(Object keyBase, long keyOffset, int keyLen,
      Object valueBase, long valueOffset, int valueLen, long prefix)
    throws IOException {

    growPointerArrayIfNecessary();
    final int required = keyLen + valueLen + 4 + 4;
    acquireNewPageIfNecessary(required);

    /**
     * ??????k-v??????currentPage(MemoryBlock)???????????????????????????pageCursor
     */
    final Object base = currentPage.getBaseObject();
    /**
     * ??????currentPage???pageCursor??????????????????codec?????????????????????????????????????????????
     * ????????????????????????
     */
    final long recordAddress = dataNodeMemoryManager.encodePageNumberAndOffset(currentPage,pageCursor);

    /**
     * ????????????????????????=keyLen + valueLen + record length (?????????int??????4?????????)
     */
    Platform.putInt(base,pageCursor, keyLen + valueLen + 4/**record length???????????????*/);

      /**
       * ??????4???bytes
       */
    pageCursor += 4;
    /**
     * ???key len???size
     */
    Platform.putInt(base,pageCursor, keyLen);

    /**
     * ??????4???bytes
     */
    pageCursor += 4;

    /**
     * ???key??????
     */
    Platform.copyMemory(keyBase, keyOffset, base, pageCursor, keyLen);
    /**
     * ??????keyLen???bytes
     */
    pageCursor += keyLen;

    /**
     * ???value??????
     */
    Platform.copyMemory(valueBase, valueOffset, base, pageCursor, valueLen);

    /**
     * ??????valueLen???bytes
     */
    pageCursor += valueLen;

    assert(inMemSorter != null);
    /**
     * ???????????????????????????longArray????????????
     * longArray?????????Page?????????????????????????????????
     */
    inMemSorter.insertRecord(recordAddress, prefix);
  }

  /**
   * Merges another UnsafeExternalSorters into this one, the other one will be emptied.
   *
   * @throws IOException
   */
  public void merge(UnsafeExternalSorter other) throws IOException {
    other.spill();
    spillWriters.addAll(other.spillWriters);
    // remove them from `spillWriters`, or the files will be deleted in `cleanupResources`.
    other.spillWriters.clear();
    other.cleanupResources();
  }

  /**
   * SpillableIterator?????????????????????+????????????????????????
   * Returns a sorted iterator. It is the caller's responsibility to call `cleanupResources()`
   * after consuming this iterator.
   */

  public UnsafeSorterIterator getSortedIterator() throws IOException {
    assert(recordComparator != null);
    if (spillWriters.isEmpty()) {
      assert(inMemSorter != null);
      readingIterator = new SpillableIterator(inMemSorter.getSortedIterator());
      return readingIterator;
    } else {
      /**
       * ????????????UnsafeSorterSpillWriter??????????????????????????????????
       */
      final UnsafeSorterSpillMerger spillMerger =
        new UnsafeSorterSpillMerger(recordComparator, prefixComparator, spillWriters.size());

      for (UnsafeSorterSpillWriter spillWriter : spillWriters) {
        /**
         * ??????UnsafeSorterSpillReader???????????????????????????UnsafeSorterSpillMerger???
         */
        spillMerger.addSpillIfNotEmpty(spillWriter.getReader(serializerManager));
      }
      if (inMemSorter != null) {
        readingIterator = new SpillableIterator(inMemSorter.getSortedIterator());
        spillMerger.addSpillIfNotEmpty(readingIterator);
      }
      /**
       * ????????????????????????????????????????????????
       */
      return spillMerger.getSortedIterator();
    }
  }

  /**
   * An UnsafeSorterIterator that support spilling.
   */
  public class SpillableIterator extends UnsafeSorterIterator {
    private UnsafeSorterIterator upstream;
    private UnsafeSorterIterator nextUpstream = null;
    private MemoryBlock lastPage = null;
    private boolean loaded = false;
    private int numRecords = 0;

    SpillableIterator(UnsafeInMemorySorter.SortedIterator inMemIterator) {
      this.upstream = inMemIterator;
      this.numRecords = inMemIterator.getNumRecords();
    }

    public int getNumRecords() {
      return numRecords;
    }

    public long spill() throws IOException {
      synchronized (this) {
        if (!(upstream instanceof UnsafeInMemorySorter.SortedIterator && nextUpstream == null
          && numRecords > 0)) {
          return 0L;
        }

        UnsafeInMemorySorter.SortedIterator inMemIterator =
          ((UnsafeInMemorySorter.SortedIterator) upstream).clone();

        // Iterate over the records that have not been returned and spill them.
        final UnsafeSorterSpillWriter spillWriter =
          new UnsafeSorterSpillWriter(blockManager, fileBufferSizeBytes,/**writeMetrics,*/ numRecords);
        while (inMemIterator.hasNext()) {
          inMemIterator.loadNext();
          final Object baseObject = inMemIterator.getBaseObject();
          final long baseOffset = inMemIterator.getBaseOffset();
          final int recordLength = inMemIterator.getRecordLength();
          spillWriter.write(baseObject, baseOffset, recordLength, inMemIterator.getKeyPrefix());
        }
        spillWriter.close();
        spillWriters.add(spillWriter);
        nextUpstream = spillWriter.getReader(serializerManager);

        long released = 0L;
        synchronized (UnsafeExternalSorter.this) {
          // release the pages except the one that is used. There can still be a caller that
          // is accessing the current record. We free this page in that caller's next loadNext()
          // call.
          for (MemoryBlock page : allocatedPages) {
            if (!loaded || page.getBaseObject() != upstream.getBaseObject()) {
              released += page.size();
              freePage(page);
            } else {
              lastPage = page;
            }
          }
          allocatedPages.clear();
        }

        // in-memory sorter will not be used after spilling
        assert(inMemSorter != null);
        released += inMemSorter.getMemoryUsage();
        totalSortTimeNanos += inMemSorter.getSortTimeNanos();
        inMemSorter.free();
        inMemSorter = null;
        totalSpillBytes += released;
        return released;
      }
    }

    @Override
    public boolean hasNext() {
      return numRecords > 0;
    }

    @Override
    public void loadNext() throws IOException {
      synchronized (this) {
        loaded = true;
        if (nextUpstream != null) {
          // Just consumed the last record from in memory iterator
          if (lastPage != null) {
            freePage(lastPage);
            lastPage = null;
          }
          upstream = nextUpstream;
          nextUpstream = null;
        }
        numRecords--;
        upstream.loadNext();
      }
    }

    @Override
    public Object getBaseObject() {
      return upstream.getBaseObject();
    }

    @Override
    public long getBaseOffset() {
      return upstream.getBaseOffset();
    }

    @Override
    public int getRecordLength() {
      return upstream.getRecordLength();
    }

    @Override
    public long getKeyPrefix() {
      return upstream.getKeyPrefix();
    }
  }

  /**
   * Returns a iterator, which will return the rows in the order as inserted.
   *
   * It is the caller's responsibility to call `cleanupResources()`
   * after consuming this iterator.
   *
   * TODO: support forced spilling
   */
  public UnsafeSorterIterator getIterator() throws IOException {
    /**
     * ??????spillWriters??????????????????????????????????????????
     */
    if (spillWriters.isEmpty()) {
      assert(inMemSorter != null);
      return inMemSorter.getSortedIterator();
    } else {
      /**
       * ?????????spillWriters?????????file?????????????????????getReader??????UnsafeSorterSpillReader???
       * ????????????????????????UnsafeSorterIterator????????????????????????queue?????????
       * UnsafeSorterSpillReader??????UnsafeSorterIterator?????????
       */
      LinkedList<UnsafeSorterIterator> queue = new LinkedList<UnsafeSorterIterator>();
      for (UnsafeSorterSpillWriter spillWriter : spillWriters) {
        queue.add(spillWriter.getReader(serializerManager));
      }
      if (inMemSorter != null) {
        queue.add(inMemSorter.getSortedIterator());
      }
      /**
       * ChainedIterator?????????UnsafeSorterIterator?????????
       * ??????????????????UnsafeSorterIterator????????????UnsafeSorterIterator
       * ?????????????????????
       */
      return new ChainedIterator(queue);
    }
  }

  /**
   * Chain multiple UnsafeSorterIterator together as single one.
   */
  static class ChainedIterator extends UnsafeSorterIterator {

    private final Queue<UnsafeSorterIterator> iterators;
    private UnsafeSorterIterator current;
    private int numRecords;

    ChainedIterator(Queue<UnsafeSorterIterator> iterators) {
      assert iterators.size() > 0;
      this.numRecords = 0;
      for (UnsafeSorterIterator iter: iterators) {
        this.numRecords += iter.getNumRecords();
      }
      this.iterators = iterators;
      this.current = iterators.remove();
    }

    @Override
    public int getNumRecords() {
      return numRecords;
    }

    @Override
    public boolean hasNext() {
      while (!current.hasNext() && !iterators.isEmpty()) {
        current = iterators.remove(); /**??????????????????????????????????????????UnsafeSorterIterator*/
      }
      return current.hasNext();
    }

    @Override
    public void loadNext() throws IOException {
      while (!current.hasNext() && !iterators.isEmpty()) {
        current = iterators.remove(); /**??????????????????????????????????????????UnsafeSorterIterator*/
      }
      current.loadNext();
    }

    @Override
    public Object getBaseObject() { return current.getBaseObject(); }

    @Override
    public long getBaseOffset() { return current.getBaseOffset(); }

    @Override
    public int getRecordLength() { return current.getRecordLength(); }

    @Override
    public long getKeyPrefix() { return current.getKeyPrefix(); }
  }
}
