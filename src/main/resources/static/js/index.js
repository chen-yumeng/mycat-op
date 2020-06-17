// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: '首页',
        data: {},
        time: null,
        MycatFirewallConfig: [],
        blackList: [],
    },
    created() {
        this.init(); //初始化
    },
    mounted() {
        this.$refs.loader.style.display = 'none';
    },
    methods: {
        /**
         * 初始化 启动
         */
        init() {
            this.$http.get(api.mycat.jvm.runtime.get).then(response => {
                this.time = this.getTime(response.body.data.startTime, new Date().getTime());
            });
            this.$http.get(api.mycat.properties.schema.getMycatSchemaConfig).then(res => {

            });
            this.getFirewall();
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
        getTime(start, now) {
            if (start != undefined && now != undefined) {
                let millisecond = now - start;
                let min = Math.floor((millisecond / 1000 / 60) << 0);
                return min;
            }
        },
    },
});
