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
package io.mycat.backend.mysql.nio;

import io.mycat.MycatServer;
import io.mycat.backend.mysql.nio.handler.ResponseHandler;
import io.mycat.config.model.DBHostConfig;
import io.mycat.net.NIOConnector;
import io.mycat.net.factory.BackendConnectionFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NetworkChannel;

/**
 * @author mycat
 */
public class MySQLConnectionFactory extends BackendConnectionFactory {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MySQLConnection make(MySQLDataSource pool, ResponseHandler handler, String schema) throws IOException {
        //DBHost配置
        DBHostConfig dsc = pool.getConfig();
        //根据是否为NIO返回SocketChannel或者AIO的AsynchronousSocketChannel
        NetworkChannel channel = openSocketChannel(MycatServer.getInstance().isAIO());

        //新建MySQLConnection
        MySQLConnection sqlConnection = new MySQLConnection(channel, pool.isReadNode());
        //根据配置初始化MySQLConnection
        MycatServer.getInstance().getConfig().setSocketParams(sqlConnection, false);
        sqlConnection.setHost(dsc.getIp());
        sqlConnection.setPort(dsc.getPort());
        sqlConnection.setUser(dsc.getUser());
        sqlConnection.setPassword(dsc.getPassword());
        sqlConnection.setSchema(schema);
        //目前实际连接还未建立，handler为MySQL连接认证MySQLConnectionAuthenticator,传入的handler为后端连接处理器ResponseHandler
        sqlConnection.setHandler(new MySQLConnectionAuthenticator(sqlConnection, handler));
        sqlConnection.setPool(pool);
        sqlConnection.setIdleTimeout(pool.getConfig().getIdleTimeout());
        //AIO和NIO连接方式建立实际的MySQL连接
        if (channel instanceof AsynchronousSocketChannel) {
            ((AsynchronousSocketChannel) channel).connect(new InetSocketAddress(dsc.getIp(), dsc.getPort()),
                    sqlConnection, (CompletionHandler) MycatServer.getInstance().getConnector());
        } else {
            //通过NIOConnector建立连接
            ((NIOConnector) MycatServer.getInstance().getConnector()).postConnect(sqlConnection);
        }
        return sqlConnection;
    }

}