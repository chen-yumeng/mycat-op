// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: 'Mycat schema配置',
        data: {},
        loading: true,
        MycatSchemaConfig: {},
        MycatDataHostsConfig: [],
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
            this.getSchemas();
            this.getDataHosts();
        },
        objToArr(obj) {
            let entries = Object.entries(obj);
            let arr = [];
            for (let i = 0; i < entries.length; i++) {
                if (entries[i] != undefined) {
                    let item = entries[i][1];
                    arr.push({
                        key: entries[i][0],
                        value: item,
                        index: i
                    });
                }
            }
            return arr;
        },
        getSchemas() {
            this.$http.get(api.mycat.properties.schema.getMycatSchemaConfig).then(response => {
                this.MycatSchemaConfig = this.objToArr(response.body.data);
                this.MycatSchemaConfig.forEach(value => {
                    value.value.tables = this.objToArr(value.value.tables);
                    value.value.tables.forEach(value => {
                        value.value = this.objToArr(value.value);
                    });
                    value.value.dataNodeDbTypeMap = this.objToArr(value.value.dataNodeDbTypeMap);
                    value.value = this.objToArr(value.value);
                });
            });
            this.loading = false;
        },
        getDataHosts() {
            this.$http.get(api.mycat.properties.schema.getMycatDataHostsConfig).then(response => {
                this.MycatDataHostsConfig = response.body.data;
                for (let i = 0; i < this.MycatDataHostsConfig.length; i++) {
                    this.MycatDataHostsConfig[i] = this.objToArr(this.MycatDataHostsConfig[i]);
                    this.MycatDataHostsConfig[i].forEach(value => {
                        if (value.key == 'writeHosts') {
                            for (let j = 0; j < value.value.length; j++) {
                                value.value[j] = this.objToArr(value.value[j]);
                            }
                        } else if (value.key == 'readHosts') {
                            if (Object.keys(value.value).length != 0) {
                                let entries = Object.entries(value.value);
                                let arr = [];
                                for (let i = 0; i < entries.length; i++) {
                                    if (entries[i] != undefined) {
                                        let item = entries[i][1];
                                        arr.push(item);
                                    }
                                }
                                value.value = arr;
                                for (let j = 0; j < value.value.length; j++) {
                                    value.value[j] = this.objToArr(value.value[j]);
                                    value.value[j].forEach(value => {
                                        value.value = this.objToArr(value.value);
                                    });
                                }
                            }
                        }
                    });
                }
            });
            this.loading = false;
        },
    },
});
