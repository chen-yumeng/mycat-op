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
package io.mycat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.config.ErrorCode;
import io.mycat.net.handler.FrontendQueryHandler;
import io.mycat.net.mysql.OkPacket;
import io.mycat.route.RouteService;
import io.mycat.server.handler.BeginHandler;
import io.mycat.server.handler.CommandHandler;
import io.mycat.server.handler.Explain2Handler;
import io.mycat.server.handler.ExplainHandler;
import io.mycat.server.handler.KillHandler;
import io.mycat.server.handler.MigrateHandler;
import io.mycat.server.handler.SavepointHandler;
import io.mycat.server.handler.SelectHandler;
import io.mycat.server.handler.SetHandler;
import io.mycat.server.handler.ShowHandler;
import io.mycat.server.handler.StartHandler;
import io.mycat.server.handler.UseHandler;
import io.mycat.server.parser.ServerParse;

/**
 * @author mycat
 */
public class ServerQueryHandler implements FrontendQueryHandler {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServerQueryHandler.class);

    private final ServerConnection source;
    protected Boolean readOnly;

    @Override
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public ServerQueryHandler(ServerConnection source) {
        this.source = source;
    }

    @Override
    public void query(String sql) {
        ServerConnection serverConnection = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(serverConnection).append(sql).toString());
        }
        int rs = ServerParse.parse(sql);
        int sqlType = rs & 0xff;

        switch (sqlType) {
            //explain sql
            case ServerParse.EXPLAIN:
                ExplainHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            //explain2 datanode=? sql=?
            case ServerParse.EXPLAIN2:
                Explain2Handler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.COMMAND:
                CommandHandler.handle(sql, serverConnection, 16);
                break;
            case ServerParse.SET:
                SetHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.SHOW:
                ShowHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.SELECT:
                SelectHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.START:
                StartHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.BEGIN:
                BeginHandler.handle(sql, serverConnection);
                break;
            //不支持oracle的savepoint事务回退点
            case ServerParse.SAVEPOINT:
                SavepointHandler.handle(sql, serverConnection);
                break;
            case ServerParse.KILL:
                KillHandler.handle(sql, rs >>> 8, serverConnection);
                break;
            //不支持KILL_Query
            case ServerParse.KILL_QUERY:
                LOGGER.warn(new StringBuilder().append("Unsupported command:").append(sql).toString());
                serverConnection.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParse.USE:
                UseHandler.handle(sql, serverConnection, rs >>> 8);
                break;
            case ServerParse.COMMIT:
                serverConnection.commit();
                break;
            case ServerParse.ROLLBACK:
                serverConnection.rollback();
                break;
            case ServerParse.HELP:
                LOGGER.warn(new StringBuilder().append("Unsupported command:").append(sql).toString());
                serverConnection.writeErrMessage(ErrorCode.ER_SYNTAX_ERROR, "Unsupported command");
                break;
            case ServerParse.MYSQL_CMD_COMMENT:
                serverConnection.write(serverConnection.writeToBuffer(OkPacket.OK, serverConnection.allocate()));
                break;
            case ServerParse.MYSQL_COMMENT:
                serverConnection.write(serverConnection.writeToBuffer(OkPacket.OK, serverConnection.allocate()));
                break;
            case ServerParse.LOAD_DATA_INFILE_SQL:
                if (RouteService.isHintSql(sql) > -1) {
                    // 目前仅支持注解 datanode,原理为直接将导入sql发送到指定mysql节点
                    serverConnection.execute(sql, ServerParse.LOAD_DATA_INFILE_SQL);
                } else {
                    serverConnection.loadDataInfileStart(sql);
                }
                break;
            case ServerParse.MIGRATE: {
                try {
                    MigrateHandler.handle(sql, serverConnection);
                } catch (Throwable e) {
                    //MigrateHandler中InterProcessMutex slaveIDsLock 会连接zk,zk连接不上会导致类加载失败,
                    // 此后再调用此命令,将会出现类未定义,所以最终还是需要重启mycat
                    e.printStackTrace();
                    String msg = "Mycat is not connected to zookeeper!!\n";
                    msg += "Please start zookeeper and restart mycat so that this mycat can temporarily execute the migration command.If other mycat does not connect to this zookeeper, they will not be able to perceive changes in the migration task.\n";
                    msg += "After starting zookeeper,you can command tas follow:\n\nmigrate -table=schema.test -add=dn2,dn3 -force=true\n\nto perform the migration.\n";
                    LOGGER.error(e.getMessage());
                    LOGGER.error(msg);
                    serverConnection.writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR, msg);
                }
                break;
            }
            case ServerParse.LOCK:
                serverConnection.lockTable(sql);
                break;
            case ServerParse.UNLOCK:
                serverConnection.unLockTable(sql);
                break;
            default:
                if (readOnly) {
                    LOGGER.warn(new StringBuilder().append("User readonly:").append(sql).toString());
                    serverConnection.writeErrMessage(ErrorCode.ER_USER_READ_ONLY, "User readonly");
                    break;
                }
                serverConnection.execute(sql, rs & 0xff);
        }

        switch (sqlType) {
            case ServerParse.SELECT:
            case ServerParse.DELETE:
            case ServerParse.UPDATE:
            case ServerParse.INSERT:
            case ServerParse.COMMAND:
                // curd 在后面会更新
                break;
            default:
                serverConnection.setExecuteSql(null);
        }
    }

}
