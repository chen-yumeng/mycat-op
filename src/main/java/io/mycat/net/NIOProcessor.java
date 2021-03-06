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
package io.mycat.net;

import io.mycat.MycatServer;
import io.mycat.backend.BackendConnection;
import io.mycat.buffer.BufferPool;
import io.mycat.statistic.CommandCount;
import io.mycat.util.NameableExecutor;
import io.mycat.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mycat
 */
public final class NIOProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("NIOProcessor");

    private final String name;
    private final BufferPool bufferPool;
    private final NameableExecutor executor;
    private final ConcurrentMap<Long, FrontendConnection> frontends;
    private final ConcurrentMap<Long, BackendConnection> backends;
    private final CommandCount commands;
    private long netInBytes;
    private long netOutBytes;

    // TODO: add by zhuam
    // reload @@config_all ???, ??????backends  ???????????? backends_old, ???????????????????????????
    public final static ConcurrentLinkedQueue<BackendConnection> backends_old = new ConcurrentLinkedQueue<BackendConnection>();

    //??????????????????
    private AtomicInteger frontendsLength = new AtomicInteger(0);

    public NIOProcessor(String name, BufferPool bufferPool, NameableExecutor executor) {
        this.name = name;
        this.bufferPool = bufferPool;
        this.executor = executor;
        this.frontends = new ConcurrentHashMap<Long, FrontendConnection>();
        this.backends = new ConcurrentHashMap<Long, BackendConnection>();
        this.commands = new CommandCount();
    }

    public String getName() {
        return name;
    }

    public BufferPool getBufferPool() {
        return bufferPool;
    }

    public int getWriteQueueSize() {
        int total = 0;
        for (FrontendConnection fron : frontends.values()) {
            total += fron.getWriteQueue().size();
        }
        for (BackendConnection back : backends.values()) {
            if (back instanceof BackendAIOConnection) {
                total += ((BackendAIOConnection) back).getWriteQueue().size();
            }
        }
        return total;

    }

    public NameableExecutor getExecutor() {
        return this.executor;
    }

    public CommandCount getCommands() {
        return this.commands;
    }

    public long getNetInBytes() {
        return this.netInBytes;
    }

    public void addNetInBytes(long bytes) {
        this.netInBytes += bytes;
    }

    public long getNetOutBytes() {
        return this.netOutBytes;
    }

    public void addNetOutBytes(long bytes) {
        this.netOutBytes += bytes;
    }

    public void addFrontend(FrontendConnection frontendConnection) {
        this.frontends.put(frontendConnection.getId(), frontendConnection);
        this.frontendsLength.incrementAndGet();
    }

    public ConcurrentMap<Long, FrontendConnection> getFrontends() {
        return this.frontends;
    }

    public int getForntedsLength() {
        return this.frontendsLength.get();
    }

    public void addBackend(BackendConnection backendConnection) {
        this.backends.put(backendConnection.getId(), backendConnection);
    }

    public ConcurrentMap<Long, BackendConnection> getBackends() {
        return this.backends;
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void checkBackendCons() {
        backendCheck();
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void checkFrontCons() {
        frontendCheck();
    }

    /**
     * ??????????????????
     */
    private void frontendCheck() {
        Iterator<Entry<Long, FrontendConnection>> it = frontends.entrySet()
                .iterator();
        while (it.hasNext()) {
            FrontendConnection frontendConnection = it.next().getValue();

            // ???????????????
            if (frontendConnection == null) {
                it.remove();
                this.frontendsLength.decrementAndGet();
                continue;
            }

            // ?????????????????????????????????????????????
            if (frontendConnection.isClosed()) {
                // ????????????????????????????????????????????????, fixed #1072  ????????????????????? #700
                //c.cleanup();
                it.remove();
                this.frontendsLength.decrementAndGet();
            } else {
                // very important ,for some data maybe not sent
                checkConSendQueue(frontendConnection);
                frontendConnection.idleCheck();
            }
        }
    }

    private void checkConSendQueue(AbstractConnection abstractConnection) {
        // very important ,for some data maybe not sent
        if (!abstractConnection.writeQueue.isEmpty()) {
            abstractConnection.getSocketWR().doNextWriteCheck();
        }
    }

    /**
     * ??????????????????
     */
    private void backendCheck() {
        long sqlTimeout = MycatServer.getInstance().getConfig().getSystem().getSqlExecuteTimeout() * 1000L;
        Iterator<Entry<Long, BackendConnection>> it = backends.entrySet().iterator();
        while (it.hasNext()) {
            BackendConnection backendConnection = it.next().getValue();

            // ???????????????
            if (backendConnection == null) {
                it.remove();
                continue;
            }
            // SQL???????????????????????????
            if (backendConnection.isBorrowed() && backendConnection.getLastTime() < TimeUtil.currentTimeMillis() - sqlTimeout) {
                LOGGER.warn("found backend connection SQL timeout ,close it " + backendConnection);
                backendConnection.close("sql timeout");
            }

            // ?????????????????????????????????????????????
            if (backendConnection.isClosed()) {
                it.remove();

            } else {
                // very important ,for some data maybe not sent
                if (backendConnection instanceof AbstractConnection) {
                    checkConSendQueue((AbstractConnection) backendConnection);
                }
                backendConnection.idleCheck();
            }
        }
    }

    public void removeConnection(AbstractConnection con) {
        if (con instanceof BackendConnection) {
            this.backends.remove(con.getId());
        } else {
            this.frontends.remove(con.getId());
            this.frontendsLength.decrementAndGet();
        }

    }

    /**
     * jdbc?????????????????????
     */
    public void removeConnection(BackendConnection con) {
        this.backends.remove(con.getId());
    }

}