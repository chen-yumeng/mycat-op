// Vue实例
let app = new Vue({
    el: '#app',
    data: {
        defaultActive: 'Mycat rule配置',
        data: {},
        loading: true,
        MycatRuleConfig: {},
        tableRule: [],
        functions: [],
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
            this.$http.get(api.mycat.properties.rule.getMycatRuleConfig).then(response => {
                this.MycatRuleConfig = response.body.data;
                this.functions = this.objToArr(this.MycatRuleConfig[0]);
                this.functions.forEach(value => {
                    value.value.rule = Object.entries(value.value.rule);
                    value.value.rule[2][1] = this.objToArr(value.value.rule[2][1]);
                    console.log(value.value);
                });
                this.tableRule = this.objToArr(this.MycatRuleConfig[1]);
                this.tableRule.forEach(value => {
                    value.value = Object.entries(value.value);
                });
            });
            this.loading = false;
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
