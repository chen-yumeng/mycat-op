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
package io.mycat.route;

import io.mycat.cache.CachePool;
import io.mycat.cache.CacheService;
import io.mycat.cache.LayerCachePool;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.route.factory.RouteStrategyFactory;
import io.mycat.route.function.PartitionByCRC32PreSlot;
import io.mycat.route.handler.HintHandler;
import io.mycat.route.handler.HintHandlerFactory;
import io.mycat.route.handler.HintSQLHandler;
import io.mycat.server.ServerConnection;
import io.mycat.server.parser.ServerParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLNonTransientException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class RouteService {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteService.class);
    public static final String MYCAT_HINT_TYPE = "_mycatHintType";
    /**
     * sql????????????
     */
    private final CachePool sqlRouteCache;
    /**
     * ?????????????????????
     */
    private final LayerCachePool tableId2DataNodeCache;

    // ???????????????????????????, ???????????????/*!mycat: type = value */ sql
    private final String OLD_MYCAT_HINT = "/*!mycat:";

    // ??????????????????:/* !mycat: type = value */ sql???oldMycatHint????????????????????????mysql
    private final String NEW_MYCAT_HINT = "/*#mycat:";
    private final String HINT_SPLIT = "=";

    public RouteService(CacheService cachService) {
        sqlRouteCache = cachService.getCachePool("SQLRouteCache");
        tableId2DataNodeCache = (LayerCachePool) cachService.getCachePool("TableID2DataNodeCache");
    }

    public LayerCachePool getTableId2DataNodeCache() {
        return tableId2DataNodeCache;
    }

    /**
     * ??????
     *
     * @param sysconf mycat?????????????????????
     * @param schema ???????????????
     * @param sqlType sql??????
     * @param stmt sql??????
     * @param charset ???????????????
     * @param sc mysql????????????
     * @return
     * @throws SQLNonTransientException
     */
    public RouteResultset route(SystemConfig sysconf, SchemaConfig schema,
                                int sqlType, String stmt, String charset, ServerConnection sc)
            throws SQLNonTransientException {
        stmt = stmt.trim();
        RouteResultset rrs = null;
        String cacheKey = null;

        /**
         * ???????????????????????????
         *  SELECT ?????????SQL, ??????
         */
        if (sqlType == ServerParse.SELECT) {
            // ?????????key
            cacheKey = schema.getName() + stmt;
            rrs = (RouteResultset) sqlRouteCache.get(cacheKey);
            //??????????????????
            if (rrs != null) {
                checkMigrateRule(schema.getName(), rrs, sqlType);
                return rrs;
            }
        }

        /*!mycat: sql = select name from aa */
        /*!mycat: schema = test */
//      boolean isMatchOldHint = stmt.startsWith(OLD_MYCAT_HINT);
//      boolean isMatchNewHint = stmt.startsWith(NEW_MYCAT_HINT);
//		if (isMatchOldHint || isMatchNewHint ) {
        int hintLength = RouteService.isHintSql(stmt);
        if (hintLength != -1) {
            int endPos = stmt.indexOf("*/");
            if (endPos > 0) {
                // ???!mycat:?????????????????????????????????
//				int hintLength = isMatchOldHint ? OLD_MYCAT_HINT.length() : NEW_MYCAT_HINT.length();
                String hint = stmt.substring(hintLength, endPos).trim();

                int firstSplitPos = hint.indexOf(HINT_SPLIT);
                if (firstSplitPos > 0) {
                    Map hintMap = parseHint(hint);
                    String hintType = (String) hintMap.get(MYCAT_HINT_TYPE);
                    String hintSql = (String) hintMap.get(hintType);
                    if (hintSql.length() == 0) {
                        LOGGER.warn("comment int sql must meet :/*!mycat:type=value*/ or /*#mycat:type=value*/ or /*mycat:type=value*/: " + stmt);
                        throw new SQLSyntaxErrorException("comment int sql must meet :/*!mycat:type=value*/ or /*#mycat:type=value*/ or /*mycat:type=value*/: " + stmt);
                    }
                    String realSQL = stmt.substring(endPos + "*/".length()).trim();

                    HintHandler hintHandler = HintHandlerFactory.getHintHandler(hintType);
                    if (hintHandler != null) {

                        if (hintHandler instanceof HintSQLHandler) {
                            /**
                             * ?????? ??????SQL??? sqlType ??? ??????SQL??? sqlType ?????????????????? ?????? hint=SELECT???real=INSERT
                             * fixed by zhuam
                             */
                            int hintSqlType = ServerParse.parse(hintSql) & 0xff;
                            rrs = hintHandler.route(sysconf, schema, sqlType, realSQL, charset, sc, tableId2DataNodeCache, hintSql, hintSqlType, hintMap);

                        } else {
                            rrs = hintHandler.route(sysconf, schema, sqlType, realSQL, charset, sc, tableId2DataNodeCache, hintSql, sqlType, hintMap);
                        }

                    } else {
                        LOGGER.warn("TODO , support hint sql type : " + hintType);
                    }

                } else {//fixed by runfriends@126.com
                    LOGGER.warn("comment in sql must meet :/*!mycat:type=value*/ or /*#mycat:type=value*/ or /*mycat:type=value*/: " + stmt);
                    throw new SQLSyntaxErrorException("comment in sql must meet :/*!mcat:type=value*/ or /*#mycat:type=value*/ or /*mycat:type=value*/: " + stmt);
                }
            }
        } else {
            stmt = stmt.trim();
            rrs = RouteStrategyFactory.getRouteStrategy().route(sysconf, schema, sqlType, stmt,
                    charset, sc, tableId2DataNodeCache);
        }

        if (rrs != null && sqlType == ServerParse.SELECT && rrs.isCacheAble()) {
            sqlRouteCache.putIfAbsent(cacheKey, rrs);
        }
        checkMigrateRule(schema.getName(), rrs, sqlType);
        return rrs;
    }

    /**
     * ??????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param schemal
     * @param rrs
     * @param sqlType
     * @throws SQLNonTransientException
     */
    private void checkMigrateRule(String schemal, RouteResultset rrs, int sqlType) throws SQLNonTransientException {
        if (rrs != null && rrs.getTables() != null) {
            boolean isUpdate = isUpdateSql(sqlType);
            if (!isUpdate) return;
            ConcurrentMap<String, List<PartitionByCRC32PreSlot.Range>> tableRules = RouteCheckRule.migrateRuleMap.get(schemal.toUpperCase());
            if (tableRules != null) {
                for (String table : rrs.getTables()) {
                    List<PartitionByCRC32PreSlot.Range> rangeList = tableRules.get(table.toUpperCase());
                    if (rangeList != null && !rangeList.isEmpty()) {
                        if (rrs.getNodes().length > 1 && isUpdate) {
                            throw new SQLNonTransientException("schema:" + schemal + ",table:" + table + ",sql:" + rrs.getStatement() + " is not allowed,because table is migrate switching,please wait for a moment");
                        }
                        for (PartitionByCRC32PreSlot.Range range : rangeList) {
                            RouteResultsetNode[] routeResultsetNodes = rrs.getNodes();
                            for (RouteResultsetNode routeResultsetNode : routeResultsetNodes) {
                                int slot = routeResultsetNode.getSlot();
                                if (isUpdate && slot >= range.start && slot <= range.end) {
                                    throw new SQLNonTransientException("schema:" + schemal + ",table:" + table + ",sql:" + rrs.getStatement() + " is not allowed,because table is migrate switching,please wait for a moment");

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isUpdateSql(int type) {
        return ServerParse.INSERT == type || ServerParse.UPDATE == type ||
                ServerParse.DELETE == type || ServerParse.DDL == type;
    }

    public static int isHintSql(String sql) {
        int j = 0;
        int len = sql.length();
        if (sql.charAt(j++) == '/' && sql.charAt(j++) == '*') {
            char c = sql.charAt(j);
            // ????????? ?????? ??? * ????????????, ????????? "/** !mycat: */" ??? "/** #mycat: */" ???????????????
            while (j < len && c != '!' && c != '#' && (c == ' ' || c == '*')) {
                c = sql.charAt(++j);
            }
            //???????????????'!'??????mysql???????????????
            //???????????????'#'??????mybatis??????
            //???????????????':'??????hibernate??????
            //?????????mycat??????????????????Hintsql:"/** mycat: */"
            if (sql.charAt(j) == 'm') {
                j--;
            }
            // prevent the following sql.charAt overflow
            if (j + 6 >= len) {
                // false
                return -1;
            }
            if (sql.charAt(++j) == 'm' && sql.charAt(++j) == 'y' && sql.charAt(++j) == 'c' && sql.charAt(++j) == 'a'
                    && sql.charAt(++j) == 't' && (sql.charAt(++j) == ':' || sql.charAt(j) == '#' || sql.charAt(j) == '-')) {
                // true????????????????????????????????????
                return j + 1;
            }
        }
        // false
        return -1;
    }

    private Map parseHint(String sql) {
        Map map = new HashMap();
        int y = 0;
        int begin = 0;
        for (int i = 0; i < sql.length(); i++) {
            char cur = sql.charAt(i);
            if (cur == ',' && y % 2 == 0) {
                String substring = sql.substring(begin, i);
                parseKeyValue(map, substring);
                begin = i + 1;
            } else if (cur == '\'') {
                y++;
            }
            if (i == sql.length() - 1) {
                parseKeyValue(map, sql.substring(begin));
            }
        }
        return map;
    }

    private void parseKeyValue(Map map, String substring) {
        int indexOf = substring.indexOf('=');
        if (indexOf != -1) {
            String key = substring.substring(0, indexOf).trim().toLowerCase();
            String value = substring.substring(indexOf + 1, substring.length());
            if (value.endsWith("'") && value.startsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            if (map.isEmpty()) {
                map.put(MYCAT_HINT_TYPE, key);
            }
            map.put(key, value.trim());

        }
    }
}
