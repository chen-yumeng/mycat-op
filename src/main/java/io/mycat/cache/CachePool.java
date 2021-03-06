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

/**
 * simple cache pool for implement
 *
 * @author wuzhih
 */
public interface CachePool {

    /**
     * 放入缓存前先用 get 方法判断是否存在
     * @param key
     * @param value
     */
    public void putIfAbsent(Object key, Object value);

    /**
     * 判断缓存的 key 是否存在
     *
     * @param key
     * @return
     */
    public Object get(Object key);

    /**
     * 清理缓存
     */
    public void clearCache();

    /**
     * 缓存状态信息
     * @return
     */
    public CacheStatic getCacheStatic();

    /**
     * 最大缓存大小
     * @return
     */
    public long getMaxSize();

    /**
     * 通过缓存名字清理缓存
     * @param cacheName
     */
    public void clearCache(String cacheName);
}