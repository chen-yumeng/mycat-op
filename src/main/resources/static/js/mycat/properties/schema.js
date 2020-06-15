// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: 'Mycat schema配置',
        data: {},
        loading: true,
        MycatSchemaConfig: {},
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
            this.$http.get(api.mycat.jvm.mycat.getMycatSchemaConfig).then(response => {
                this.MycatSchemaConfig = response.body.data;
            });
        },
        // getMycat(MycatSystemConfig) {
        //     let entries = Object.entries(MycatSystemConfig);
        //     let mycat = [];
        //     entries.forEach(value => {
        //         let item = {
        //             key: value[0]+"",
        //             value: value[1]+""
        //         };
        //         mycat.push(item);
        //     });
        //     return mycat;
        // },
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
