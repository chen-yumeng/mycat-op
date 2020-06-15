// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: '概述',
        data: {},
        loading: true,
        MycatSystemConfig: {},
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
            this.$http.get(api.mycat.jvm.runtime.get).then(response => {
                this.data = response.body.data;
                this.loading = false;
            });
            this.$http.get(api.mycat.properties.server.getMycatSystemConfig).then(response => {
                this.MycatSystemConfig = this.getMycat(response.body.data);
            });
        },
        getMycat(MycatSystemConfig) {
            let entries = Object.entries(MycatSystemConfig);
            let mycat = [];
            entries.forEach(value => {
                mycat.push({
                    key: value[0]+"",
                        value: value[1]+""
                });
            });
            return mycat;
        },
        strFormat(value) {
            return value.replace(/[;:]/g, "<br/>");
        },
        dateFormat(millisecond) {
            if (millisecond != undefined) {
                return (new Date(millisecond).toLocaleDateString()) + " " + (new Date(millisecond).toTimeString());
            }
        }
    },
});
