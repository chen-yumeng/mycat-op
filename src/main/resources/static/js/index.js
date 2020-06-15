// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: '首页',
        data: {},
        time: null,

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
