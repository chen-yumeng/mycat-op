// Vue实例
var app = new Vue({
    el: '#app',
    data: {
        defaultActive: '首页',
        data: {},
        time: null,
        MycatFirewallConfig: [],
        blackList: [],
        dataNodes: [],
        schemaCount : 0,
        memory: null,
        loading: true,
    },
    created() {
        this.init(); //初始化
    },
    mounted() {
        this.$refs.loader.style.display = 'none';
    },
    watch: {
        memory() {
            this.timer();
        }
    },
    destroyed() {
        clearTimeout(this.timer)
    },
    methods: {
        /**
         * 初始化 启动
         */
        init() {
            this.getTime();
            this.getFirewall();
            this.getDataNodes();
            this.getSchema();
            this.getMemory();
            this.loading = false;
        },
        timer() {
            return setTimeout(()=>{
                this.getMemory();
                this.getTime();
            },6e3)
        },
        getMemory() {
            this.$http.get(api.mycat.jvm.memory.get).then(response => {
                if (response.body.code == 200) {
                    this.memory = (response.body.data.used / 1024 / 1024).toFixed(2);
                }
            })
        },
        getSchema() {
            this.$http.get(api.mycat.properties.schema.getMycatSchemaConfig).then(res => {
                this.schemaCount = Object.entries(res.body.data).length;
            });
        },
        getDataNodes() {
            this.$http.get(api.mycat.properties.schema.getMycatDataNodesConfig).then(res => {
                this.dataNodes = res.body.data;
            });
        },
        getFirewall() {
            this.$http.get(api.mycat.properties.server.getMycatAllFirewallConfig).then(response => {
                let whiteHost = this.objToArr(response.body.data.whitehost);
                let whiteHostMask = this.replaceIP(this.objToArr(response.body.data.whitehostMask));
                whiteHost.forEach(value => {
                    this.MycatFirewallConfig.push(value);
                });
                whiteHostMask.forEach(value => {
                    this.MycatFirewallConfig.push(value);
                });
                this.blackList = this.objToArr(response.body.data.blacklist);
            });
        },
        replaceIP(whiteHostMask, index) {
            if (index == 1) {
                whiteHostMask.key = whiteHostMask.key.replace(/\*/g, '[0-9]*').replace(/\./g, '\\\.');
                return whiteHostMask;
            }
            whiteHostMask.forEach(value => {
                value.key = value.key.replace(/\[0-9\]\*/g, '*').replace(/\\\./g, '.');
            });
            return whiteHostMask;
        },
        objToArr(obj) {
            let entries = Object.entries(obj);
            let arr = [];
            for (let i = 0; i < entries.length; i++) {
                if (entries[i] != undefined) {
                    let names = [];
                    let item = entries[i][1];
                    if (item instanceof Array) {
                        item.forEach(value => {
                            names.push(value.name);
                        });
                    }
                    arr.push({
                        key: entries[i][0],
                        value: item,
                        names: names,
                        index: i
                    });
                }
            }
            return arr;
        },
        getTime() {
            this.$http.get(api.mycat.jvm.runtime.get).then(response => {
                let millisecond = new Date().getTime() - response.body.data.startTime;
                let min = Math.floor((millisecond / 1000 / 60) << 0);
                this.time = min;
            });
        },
    },
});
