//设置全局表单提交格式
Vue.http.options.emulateJSON = true;

//前端API访问接口
let api = {
    mycat : {
        jvm: {
            runtime: {
                get: '/mycat/runtime/get',
            },
            class: {
                get: '/mycat/class/get',
            },
            memory: {
                get: '/mycat/memory/get',
            },
            thread: {
                get: '/mycat/thread/get'
            },
            gc: {
                get: '/mycat/gc/get',
                getPools: '/mycat/gc/getPools'
            },
        },
        properties: {
            server: {
                getMycatSystemConfig: '/mycat/properties/getMycatSystemConfig',
                getMycatUsersConfig: '/mycat/properties/getMycatUsersConfig',
                getMycatAllFirewallConfig: '/mycat/properties/getMycatAllFirewallConfig',
                editMycatSystemConfig: '/mycat/properties/editMycatSystemConfig',
            },
            schema: {
                getMycatSchemaConfig: '/mycat/properties/getMycatSchemaConfig',
            },
            rule: {
                getMycatRuleConfig: '/mycat/properties/getMycatRuleConfig',
            }
        }
    },

};