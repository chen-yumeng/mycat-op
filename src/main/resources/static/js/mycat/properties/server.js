// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: 'Mycat server配置',
        data: {},
        loading: true,
        MycatSystemConfig: {},
        users: [],
        MycatFirewallConfig: {},
    },
    created() {
        this.init(); //初始化
    },
    mounted() {
        this.$refs.loader.style.display = 'none';
    },
    methods: {
        /**
         * 初始化
         */
        init() {
            this.loading = true;
            this.$http.get(api.mycat.properties.server.getMycatSystemConfig).then(response => {
                this.MycatSystemConfig = this.filterSystem(response.body.data);
                this.loading = false;
            });
            this.$http.get(api.mycat.properties.server.getMycatUsersConfig).then(response => {
                this.getUsers(response.body.data);
                this.loading = false;
            });
            this.$http.get(api.mycat.properties.server.getMycatAllFirewallConfig).then(response => {
                this.MycatFirewallConfig = response.body.data;
                this.loading = false;
            });
        },
        getUsers(config) {
            let entries = Object.entries(config);
            for (let i = 0; i < entries.length; i++) {
                if (entries[i] != undefined) {
                    this.users.push({
                        key: entries[i][0],
                        value: this.filterUser(entries[i][1])
                    });
                }
            }
        },
        strFormat(value) {
            return value.replace(/[;:]/g, "<br/>");
        },
        editSystem(item) {
            this.$http.post(api.mycat.properties.server.editMycatSystemConfig, {
                key: item.key,
                value: item.value
            }).then(res => {
                if (res.body.data) {
                    this.$message({
                        message: '修改成功!',
                        type: 'success'
                    });
                }
            })
        },
        filterUser(user) {
            let u = [];
            u.push({
                name: "用户名",
                key: "name",
                value: user.name
            });
            u.push({
                name: "密码",
                key: "password",
                value: user.password
            });
            u.push({
                name: "用户sql权限配置",
                key: "privilegesConfig",
                value: JSON.stringify(user.privilegesConfig)
            });
            u.push({
                name: "负载限制, 默认0表示不限制",
                key: "benchmark",
                value: user.benchmark
            });
            u.push({
                name: "是否无密码登陆的默认账户",
                key: "defaultAccount",
                value: user.defaultAccount
            });
            u.push({
                name: "加密密码",
                key: "encryptPassword",
                value: user.encryptPassword
            });
            u.push({
                name: "是否只读",
                key: "readOnly",
                value: user.readOnly
            });
            u.push({
                name: "默认所管理的逻辑库",
                key: "defaultSchema",
                value: user.defaultSchema
            });
            u.push({
                name: "所管理的逻辑库",
                key: "schemas",
                value: user.schemas
            });
            return u;
        },
        filterSystem(config) {
            let mycat = [];
            mycat.push({
                name: "是否需要密码登陆(0为需要密码登陆、1为不需要密码登陆 ,默认为0，设置为1则需要指定默认账户)",
                key: "nonePasswordLogin",
                value: config.nonePasswordLogin + ""
            });
            mycat.push({
                name: "是否忽略报文(0遇上没有实现的报文(Unknown command:),就会报错、1为忽略该报文，返回ok报文。)",
                key: "ignoreUnknownCommand",
                value: config.ignoreUnknownCommand + ""
            });
            mycat.push({
                name: "启动sql统计(1为开启实时统计、0为关闭)",
                key: "useSqlStat",
                value: config.useSqlStat + ""
            });
            mycat.push({
                name: "使用全局表检查(1为开启全加表一致性检测、0为关闭)",
                key: "useGlobleTableCheck",
                value: config.useGlobleTableCheck + ""
            });
            mycat.push({
                name: "SQL 执行超时 单位:秒",
                key: "sqlExecuteTimeout",
                value: config.sqlExecuteTimeout + ""
            });
            mycat.push({
                name: "序列处理程序类型",
                key: "sequenceHandlerType",
                value: config.sequenceHandlerType + ""
            });
            mycat.push({
                name: "序列处理程序模式(必须带有MYCATSEQ_或者 mycatseq_进入序列匹配流程 注意MYCATSEQ_有空格的情况)",
                key: "sequnceHandlerPattern",
                value: config.sequnceHandlerPattern + ""
            });
            mycat.push({
                name: "子查询关系检查(子查询中存在关联查询的情况下,检查关联字段中是否有分片字段.默认 false)",
                key: "subqueryRelationshipCheck",
                value: config.subqueryRelationshipCheck + ""
            });
            mycat.push({
                name: "序列处理程序类",
                key: "sequenceHanlderClass",
                value: config.sequenceHanlderClass + ""
            });
            mycat.push({
                name: "是否开启mysql压缩协议(1为开启mysql压缩协议)",
                key: "useCompression",
                value: config.useCompression + ""
            });
            mycat.push({
                name: "设置模拟的MySQL版本号",
                key: "fakeMySQLVersion",
                value: config.fakeMySQLVersion + ""
            });
            mycat.push({
                name: "处理器个数",
                key: "processors",
                value: config.processors + ""
            });
            mycat.push({
                name: "处理器执行器个数",
                key: "processorExecutor",
                value: config.processorExecutor + ""
            });
            mycat.push({
                name: "处理器缓冲池类型(默认为type 0: DirectByteBufferPool | type 1 ByteBufferArena | type 2 NettyBufferPool)",
                key: "processorBufferPoolType",
                value: config.processorBufferPoolType + ""
            });
            mycat.push({
                name: "sql解析时最大文本长度(默认是65535 64K)",
                key: "maxStringLiteralLength",
                value: config.maxStringLiteralLength + ""
            });
            mycat.push({
                name: "后端通信是否无延迟",
                key: "backSocketNoDelay",
                value: config.backSocketNoDelay + ""
            });
            mycat.push({
                name: "前端通信是否无延迟",
                key: "frontSocketNoDelay",
                value: config.frontSocketNoDelay + ""
            });
            mycat.push({
                name: "服务端端口",
                key: "serverPort",
                value: config.serverPort + ""
            });
            mycat.push({
                name: "管理端端口",
                key: "managerPort",
                value: config.managerPort + ""
            });
            mycat.push({
                name: "空闲超时",
                key: "idleTimeout",
                value: config.idleTimeout + ""
            });
            mycat.push({
                name: "连接空闲检查(默认5 * 60 * 1000L)",
                key: "dataNodeIdleCheckPeriod",
                value: config.dataNodeIdleCheckPeriod + ""
            });
            mycat.push({
                name: "绑定的ip",
                key: "bindIp",
                value: config.bindIp + ""
            });
            mycat.push({
                name: "前端写队列大小",
                key: "frontWriteQueueSize",
                value: config.frontWriteQueueSize + ""
            });
            mycat.push({
                name: "分布式事务开关(0为不过滤分布式事务，1为过滤分布式事务（如果分布式事务内只涉及全局表，则不过滤），2为不过滤分布式事务,但是记录分布式事务日志)",
                key: "handleDistributedTransactions",
                value: config.handleDistributedTransactions + ""
            });
            mycat.push({
                name: "是否开启流式查询控制(默认开启)",
                key: "enableFlowControl",
                value: config.enableFlowControl + ""
            });
            mycat.push({
                name: "流式查询控制启动的写队列最大值(默认为jvm内存最大值/bufferPoolChunkSize*0.618/dataNode的数量)",
                key: "flowControlStartMaxValue",
                value: config.flowControlStartMaxValue + ""
            });
            mycat.push({
                name: "流式查询控制关闭的写队列最大值(默认为flowControlStartMaxValue/5)",
                key: "flowControlStopMaxValue",
                value: config.flowControlStopMaxValue + ""
            });
            mycat.push({
                name: "缓冲池最小分配单位(默认4096)",
                key: "bufferPoolChunkSize",
                value: config.bufferPoolChunkSize + ""
            });
            mycat.push({
                name: "缓冲池每页大小(默认512 * 1024 * 4)",
                key: "bufferPoolPageSize",
                value: config.bufferPoolPageSize + ""
            });
            mycat.push({
                name: "缓冲池总页数(默认processors*20)",
                key: "bufferPoolPageNumber",
                value: config.bufferPoolPageNumber + ""
            });
            mycat.push({
                name: "使用非堆合并(off heap for merge/order/group/limit      1开启   0关闭)",
                key: "useOffHeapForMerge",
                value: config.useOffHeapForMerge + ""
            });
            mycat.push({
                name: "内存页面大小(单位为m)",
                key: "memoryPageSize",
                value: config.memoryPageSize + ""
            });
            mycat.push({
                name: "溢出文件缓冲区大小(单位为k)",
                key: "spillsFileBufferSize",
                value: config.spillsFileBufferSize + ""
            });
            mycat.push({
                name: "使用流输出",
                key: "useStreamOutput",
                value: config.useStreamOutput + ""
            });
            mycat.push({
                name: "系统保留的内存大小(单位为m)",
                key: "systemReserveMemorySize",
                value: config.systemReserveMemorySize + ""
            });
            mycat.push({
                name: "是否采用zookeeper协调切换(默认为false)",
                key: "useZKSwitch",
                value: config.useZKSwitch + ""
            });
            mycat.push({
                name: "XA Recovery Log日志路径",
                key: "XARecoveryLogBaseDir",
                value: config.xarecoveryLogBaseDir + ""
            });
            mycat.push({
                name: "XA Recovery Log日志名称",
                key: "XARecoveryLogBaseName",
                value: config.xarecoveryLogBaseName + ""
            });
            mycat.push({
                name: "是否开启严格的事务隔离(如果为true的话,严格遵守隔离级别,不会在仅仅只有select语句的时候在事务中切换连接)",
                key: "strictTxIsolation",
                value: config.strictTxIsolation + ""
            });
            mycat.push({
                name: "是否开启并行执行(如果为0的话,涉及多个DataNode的catlet任务不会跨线程执行)",
                key: "parallExecute",
                value: config.parallExecute + ""
            });
            return mycat;
        }
    },
});
