//设置全局表单提交格式
Vue.http.options.emulateJSON = true;

//前端API访问接口
let api = {
    mycat : {
        login: {
            login: '/mycat/login',
        },
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
                getMycatSystemConfig: '/mycat/properties/server/getMycatSystemConfig',
                getMycatUsersConfig: '/mycat/properties/server/getMycatUsersConfig',
                getMycatAllFirewallConfig: '/mycat/properties/server/getMycatAllFirewallConfig',
                addBlackItem: '/mycat/properties/server/addBlackItem',
                addWhiteHostItem: '/mycat/properties/server/addWhiteHostItem',
                addWhiteHostMaskItem: '/mycat/properties/server/addWhiteHostMaskItem',
                editWhiteHostItem: '/mycat/properties/server/editWhiteHostItem',
                editWhiteHostMaskItem: '/mycat/properties/server/editWhiteHostMaskItem',
                editBlackItem: '/mycat/properties/server/editBlackItem',
                deleWhiteHostItem: '/mycat/properties/server/deleWhiteHostItem',
                deleWhiteHostMaskItem: '/mycat/properties/server/deleWhiteHostMaskItem',
                deleBlackItem: '/mycat/properties/server/deleBlackItem',
                editMycatSystemConfig: '/mycat/properties/server/editMycatSystemConfig',
            },
            schema: {
                getMycatSchemaConfig: '/mycat/properties/schema/getMycatSchemaConfig',
                getMycatDataHostsConfig: '/mycat/properties/schema/getMycatDataHostsConfig',
                getMycatDataNodesConfig: '/mycat/properties/schema/getMycatDataNodesConfig',
            },
            rule: {
                getMycatRuleConfig: '/mycat/properties/rule/getMycatRuleConfig',
            }
        }
    },

};