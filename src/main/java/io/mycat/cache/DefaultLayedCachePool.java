/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分层缓存池，LayerCachePool 的实现
 */
public class DefaultLayedCachePool implements LayerCachePool {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultLayedCachePool.class);
    /**
     * 所有缓存
     */
    protected Map<String, CachePool> allCaches = new HashMap<String, CachePool>();
    protected final ReentrantLock lock = new ReentrantLock();
    /**
     * 默认缓存大小
     */
    protected int defaultCacheSize;
    /**
     * 默认到期秒数
     */
    protected int defaulExpiredSeconds;
    /**
     * 默认缓存
     */
    protected static final String defaultCache = "default";
    public static final String DEFAULT_CACHE_COUNT = "DEFAULT_CACHE_COUNT";
    public static final String DEFAULT_CACHE_EXPIRE_SECONDS = "DEFAULT_CACHE_EXPIRE_SECONDS";
    /**
     * 缓存池工厂
     */
    private final CachePoolFactory poolFactory;
    private final String name;

    public DefaultLayedCachePool(String name, CachePoolFactory poolFactory,
                                 int defaultCacheSize, int defaulExpiredSeconds) {
        super();
        this.name = name;
        this.poolFactory = poolFactory;
        this.defaultCacheSize = defaultCacheSize;
        this.defaulExpiredSeconds = defaulExpiredSeconds;
    }

    private CachePool getCache(String cacheName) {
        CachePool pool = allCaches.get(cacheName);
        if (pool == null) {
            lock.lock();
            try {
                pool = allCaches.get(cacheName);
                if (pool == null) {
                    pool = this.createChildCache(cacheName,
                            this.defaultCacheSize, this.defaulExpiredSeconds);
                }

            } finally {
                lock.unlock();
            }
        }
        return pool;
    }

    /**
     * create child cache at runtime
     * 在运行时创建子缓存
     *
     * @param cacheName
     * @return
     */
    public CachePool createChildCache(String cacheName, int size,
                                      int expireSeconds) {
        LOGGER.info("create child Cache: " + cacheName + " for layered cache " + name + ", size " + size + ", expire seconds " + expireSeconds);
        CachePool child = this.poolFactory.createCachePool(name + "."
                + cacheName, size, expireSeconds);
        allCaches.put(cacheName, child);
        return child;
    }

    @Override
    public void putIfAbsent(Object key, Object value) {
        putIfAbsent(defaultCache, key, value);

    }

    @Override
    public Object get(Object key) {
        return get(defaultCache, key);
    }

    @Override
    public void clearCache() {
        LOGGER.info("clear cache ");
        for (CachePool pool : allCaches.values()) {
            pool.clearCache();
        }

    }

    @Override
    public void putIfAbsent(String primaryKey, Object secondKey, Object value) {
        CachePool pool = getCache(primaryKey);
        pool.putIfAbsent(secondKey, value);

    }

    @Override
    public Object get(String primaryKey, Object secondKey) {
        CachePool pool = getCache(primaryKey);
        return pool.get(secondKey);
    }

    @Override
    public CacheStatic getCacheStatic() {
        CacheStatic cacheStatic = new CacheStatic();
        cacheStatic.setMaxSize(this.getMaxSize());
        for (CacheStatic singleStatic : getAllCacheStatic().values()) {
            cacheStatic.setItemSize(cacheStatic.getItemSize() + singleStatic.getItemSize());
            cacheStatic.setHitTimes(cacheStatic.getHitTimes() + singleStatic.getHitTimes());
            cacheStatic.setAccessTimes(cacheStatic.getAccessTimes() + singleStatic.getAccessTimes());
            cacheStatic.setPutTimes(cacheStatic.getPutTimes() + singleStatic.getPutTimes());
            if (cacheStatic.getLastAccesTime() < singleStatic.getLastAccesTime()) {
                cacheStatic.setLastAccesTime(singleStatic.getLastAccesTime());
            }
            if (cacheStatic.getLastPutTime() < singleStatic.getLastPutTime()) {
                cacheStatic.setLastPutTime(singleStatic.getLastPutTime());
            }

        }
        return cacheStatic;
    }

    @Override
    public Map<String, CacheStatic> getAllCacheStatic() {
        Map<String, CacheStatic> results = new HashMap<String, CacheStatic>(
                this.allCaches.size());
        for (Map.Entry<String, CachePool> entry : allCaches.entrySet()) {
            results.put(entry.getKey(), entry.getValue().getCacheStatic());
        }
        return results;
    }

    @Override
    public long getMaxSize() {
        long maxSize = 0;
        for (CachePool cache : this.allCaches.values()) {
            maxSize += cache.getMaxSize();
        }
        return maxSize;
    }

    //clear cache by cacheName
    public void clearCache(String cacheName) {
        LOGGER.info("clear cache :" + cacheName);
        CachePool pool = getCache(cacheName);
        pool.clearCache();
    }
}