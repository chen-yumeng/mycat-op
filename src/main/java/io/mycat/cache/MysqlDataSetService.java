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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据结果集缓存服务类
 */
public class MysqlDataSetService {
	/**
	 * 是否启用
	 */
	private volatile boolean enabled = false;
	/**
	 * max expire time is 300 seconds
	 * 最大过期时间为300秒
	 */
	private int maxExpire = 300;
	private final ConcurrentHashMap<String, MysqlDataSetCache> cachedMap = new ConcurrentHashMap<String, MysqlDataSetCache>();
	/**
	 * 需要缓存的SQL
	 */
	private volatile Set<String> needCachedSQL = new HashSet<String>();

	/**
	 * 是否启用
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMaxExpire() {
		return maxExpire;
	}

	public void setMaxExpire(int maxExpire) {
		this.maxExpire = maxExpire;
	}

	private static MysqlDataSetService instance = new MysqlDataSetService();

	public static MysqlDataSetService getInstance() {
		return instance;
	}

	private MysqlDataSetService() {

	}

	/**
	 * 查找数据集缓存
	 * sql should not include LIMIT range
	 * sql不应包含LIMIT范围
	 * 
	 * @param sql
	 * @return
	 */
	public MysqlDataSetCache findDataSetCache(String sql) {
		if (!enabled) {
			return null;
		}
		MysqlDataSetCache cache = cachedMap.get(sql);
		if (validCache(cache)) {
			return cache;
		} else {
			cachedMap.remove(sql);
		}

		return null;

	}

	public String needCache(String sql)
	{
		return needCachedSQL.contains(sql)?sql:null;
	}
	public boolean addIfNotExists(MysqlDataSetCache newCache) {
		return (cachedMap.putIfAbsent(newCache.getSql(), newCache) == null);
	}

	/**
	 * 是否为有效缓存
	 * @param cache
	 * @return
	 */
	private boolean validCache(MysqlDataSetCache cache) {
		return (!cache.isStoring()
				&& (cache.getCreateTime() + this.maxExpire * 1000 < System.currentTimeMillis())
				&& (new File(cache.getDataFile()).exists()));
	}
}