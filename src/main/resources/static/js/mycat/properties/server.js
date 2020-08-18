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
        whiteHost: null,
        whiteHostMask: null,
        blackList: null,
        addBlackItem: {},
        isAddBlackItem: false,
        addWhiteHost: {},
        isAddWhiteHost: false,
        addWhiteHostMask: {},
        isAddWhiteHostMask: false,
        dialogFormVisible: false,
        userForm: {
            privilegesConfig: {
                check: false,
                schemaPrivileges: [],
                dataNodePrivileges: []
            }
        },
        editUserKey: '',
        schemas: [],
        schemaPrivilegesDialog: false,
        schemaPrivileges: {
            tablePrivileges: []
        },
        editSchemaPrivileges: '',
        schemaName: '',
        tables: [],
        tablePrivilegesDialog: false,
        tablePrivileges: {},
        editTablePrivileges: '',
        dataNodePrivilegesDialog: false,
        dataNodePrivileges: {},
        editDataNodePrivileges: "",
        dataNodes: [],
        dmlList: ["insert", "update", "select", "delete"]
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
            this.getConfig();
            this.getUsers();
            this.getFirewall();
            this.getSchemasAndTablesAndDataNode()
        },
        getSchemasAndTablesAndDataNode() {
            this.$http.get(api.mycat.properties.schema.getMycatSchemaConfig).then(res => {
                let arr = Object.entries(res.data.data);
                for (let i = 0; i < arr.length; i++) {
                    let tabArr = Object.entries(arr[i][1].tables);
                    let tables = []
                    for (let j = 0; j < tabArr.length; j++) {
                        tables.push(tabArr[j][1])
                    }
                    arr[i][1].tableList = tables
                    this.schemas.push(arr[i][1]);
                }
            })
            this.$http.get(api.mycat.properties.schema.getMycatDataNodesConfig).then(res => {
                this.dataNodes = res.data.data
            })
        },
        addPrivileges(index) {
            // 1逻辑库 2逻辑表
            if (index == 1) {
                if (this.userForm.schemas.length === 0) {
                    this.$message({message: '请先选择逻辑库!', type: 'error'});
                    return;
                }
                this.schemaPrivilegesDialog = true
            } else {
                if (this.schemaPrivileges.name === undefined) {
                    this.$message({message: '请先选择逻辑库!', type: 'error'});
                    return;
                }
                this.tablePrivilegesDialog = true
                this.schemaName = this.schemaPrivileges.name
            }
        },
        tablePrivilegesDialogOpen() {
            for (let i = 0; i < this.schemas.length; i++) {
                if (this.schemas[i].name === this.schemaName) {
                    this.tables = this.schemas[i].tableList
                }
            }
        },
        saveDataNodePrivileges() {
            if (this.editDataNodePrivileges === '') {
                this.userForm.privilegesConfig.dataNodePrivileges.push(this.dataNodePrivileges)
            }
            this.handleCloseInnerDataNode()
        },
        saveTablePrivileges() {
            if (this.editTablePrivileges === '') {
                this.schemaPrivileges.tablePrivileges.push(this.tablePrivileges)
            }
            this.handleCloseTableInner()
        },
        saveSchemaPrivileges() {
            if (this.editSchemaPrivileges === '') {
                this.userForm.privilegesConfig.schemaPrivileges.push(this.schemaPrivileges)
            }
            this.handleCloseInnerSchema()
        },
        deleteTablePrivileges(name) {
            for (let i = 0; i < this.schemaPrivileges.tablePrivileges.length; i++) {
                if (this.schemaPrivileges.tablePrivileges[i].name == name) {
                    this.schemaPrivileges.tablePrivileges.splice(i, 1)
                }
            }
        },
        deleteSchemaPrivileges(name) {
            for (let i = 0; i < this.userForm.privilegesConfig.schemaPrivileges.length; i++) {
                if (this.userForm.privilegesConfig.schemaPrivileges[i].name == name) {
                    this.userForm.privilegesConfig.schemaPrivileges.splice(i, 1)
                }
            }
        },
        deleteDataNodePrivileges(name) {
            for (let i = 0; i < this.userForm.privilegesConfig.dataNodePrivileges.length; i++) {
                if (this.userForm.privilegesConfig.dataNodePrivileges[i].name == name) {
                    this.userForm.privilegesConfig.dataNodePrivileges.splice(i, 1)
                }
            }
        },
        handleCloseInnerDataNode() {
            this.dataNodePrivilegesDialog = false
            this.editDataNodePrivileges = ''
            this.dataNodePrivileges = {}
        },
        handleCloseTableInner() {
            this.tablePrivilegesDialog = false
            this.editTablePrivileges = ''
            this.schemaName = ''
            this.tablePrivileges = {}
        },
        handleCloseInnerSchema() {
            this.schemaPrivilegesDialog = false
            this.editSchemaPrivileges = ''
            this.schemaPrivileges = {tablePrivileges: []}
        },
        handleClose() {
            this.editUserKey = ''
            this.dialogFormVisible = false
            this.userForm = {privilegesConfig: {check: false, schemaPrivileges: [], dataNodePrivileges: []}}
        },
        editUser(key) {
            this.editUserKey = key
            this.$http.get(api.mycat.properties.server.getMycatUsersConfig).then(res => {
                this.userForm = res.data.data[key]
                this.filterPrivilegesConfig(this.userForm.privilegesConfig)
            })
        },
        saveUser() {
            let config = {
                name:this.userForm.name,
                password: this.userForm.password,
                encryptPassword: this.userForm.password,
                benchmark: this.userForm.benchmark,
                defaultSchema: this.userForm.defaultSchema,
                defaultAccount: this.userForm.defaultAccount,
                readOnly: this.userForm.readOnly,
                schemas: this.userForm.schemas,
                privilegesConfig: {
                    check: this.userForm.privilegesConfig.check,
                    schemaPrivileges: {
                    },
                    dataNodePrivileges: {
                    }
                }
            }
            for (let i = 0; i < this.userForm.privilegesConfig.schemaPrivileges.length; i++) {
                let schemaKey = this.userForm.privilegesConfig.schemaPrivileges[i].name
                config.privilegesConfig.schemaPrivileges[schemaKey] = {}
                config.privilegesConfig.schemaPrivileges[schemaKey].dmlList = this.userForm.privilegesConfig.schemaPrivileges[i].dml
                config.privilegesConfig.schemaPrivileges[schemaKey].name = schemaKey
                config.privilegesConfig.schemaPrivileges[schemaKey].tablePrivileges = {}
                for (let j = 0; j < this.userForm.privilegesConfig.schemaPrivileges[i].tablePrivileges.length; j++) {
                    let tableKey = this.userForm.privilegesConfig.schemaPrivileges[i].tablePrivileges[j].name
                    config.privilegesConfig.schemaPrivileges[schemaKey].tablePrivileges[tableKey] = {}
                    config.privilegesConfig.schemaPrivileges[schemaKey].tablePrivileges[tableKey].dmlList = this.userForm.privilegesConfig.schemaPrivileges[i].tablePrivileges[j].dml
                    config.privilegesConfig.schemaPrivileges[schemaKey].tablePrivileges[tableKey].name = tableKey
                }
            }
            for (let i = 0; i < this.userForm.privilegesConfig.dataNodePrivileges.length; i++) {
                let dataNodeKey = this.userForm.privilegesConfig.dataNodePrivileges[i].name
                config.privilegesConfig.dataNodePrivileges[dataNodeKey] = {}
                config.privilegesConfig.dataNodePrivileges[dataNodeKey].name = dataNodeKey
                config.privilegesConfig.dataNodePrivileges[dataNodeKey].dmlList = this.userForm.privilegesConfig.dataNodePrivileges[i].dml
            }
            // 保存
            if (this.editUserKey==='') {
                this.$http.post(api.mycat.properties.server.addUserItem, {config:JSON.stringify(config)}).then(res => {
                    if (res.data.code === 200) {
                        this.$message({
                            type: "success",
                            message: "添加成功"
                        })
                    } else {
                        this.$message({
                            type: "error",
                            message: "添加失败"
                        })
                    }
                })
            } else {
            //  修改
                this.$http.post(api.mycat.properties.server.editUserItem, {config: JSON.stringify(config),oldName:this.editUserKey}).then(res => {
                    if (res.data.code === 200) {
                        this.$message({
                            type: "success",
                            message: "修改成功"
                        })
                    } else {
                        this.$message({
                            type: "error",
                            message: "修改失败"
                        })
                    }
                })
            }
            this.users = []
            this.getUsers()
            this.handleClose()
        },
        deleteUser(key) {
            const h = this.$createElement;
            this.$msgbox({
                title: '消息',
                message: h('p', null, [
                    h('span', null, "确认删除  "),
                    h('b', {style: 'color: teal'}, key),
                    h('span', null, '  的用户吗?')
                ]),
                showCancelButton: true,
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                beforeClose: (action, instance, done) => {
                    if (action === 'confirm') {
                        instance.confirmButtonLoading = true;
                        instance.confirmButtonText = '执行中...';
                        setTimeout(() => {
                            done();
                            setTimeout(() => {
                                instance.confirmButtonLoading = false;
                            }, 300);
                            this.$http.get(api.mycat.properties.server.deleUserItem + '?key=' + key).then(res => {
                                if (res.body.code == 200 && res.body.data) {
                                    this.$message({
                                        message: '删除成功!',
                                        type: 'success'
                                    });
                                } else {
                                    this.$message({
                                        message: '删除失败!',
                                        type: 'error'
                                    });
                                }
                                this.users = []
                                this.getUsers()
                            })
                        }, 3000);
                    } else {
                        done();
                    }
                }
            });
        },
        cheackIP(ip, index) {
            let re;
            if (index == 2) {
                re = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]|\*\d\d|\d\*\d|\d\d\*|\*|\*\d|\d\*)\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]|\*\d\d|\d\*\d|\d\d\*|\*|\*\d|\d\*)\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]|\*\d\d|\d\*\d|\d\d\*|\*|\*\d|\d\*)\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]|\*\d\d|\d\*\d|\d\d\*|\*|\*\d|\d\*)$/;
            } else {
                re = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
            }
            return re.test(ip);
        },
        replaceIP(whiteHostMask, index) {
            if (index == 1) {
                whiteHostMask.key = whiteHostMask.key.replace(/\*/g, '[0-9]*').replace(/\./g, '\\\.');
                return whiteHostMask;
            }
            for (let i = 0; i < whiteHostMask.length; i++) {
                whiteHostMask[i].key = whiteHostMask[i].key.replace(/\[0-9\]\*/g, '*').replace(/\\\./g, '.');
            }
        },
        editItem(item, index, i) {
            if (index == 3) {
                if (item.key == undefined) {
                    this.$message({
                        message: '请输入黑名单名称!',
                        type: 'error'
                    });
                    return;
                }
                if (!this.cheackIP(item.value, index)) {
                    this.$message({
                        message: 'IP地址不合法!',
                        type: 'error'
                    });
                    return;
                }
            } else if (index == 0) {
            } else {
                if (item.names.length <= 0) {
                    this.$message({
                        message: '请选择对应用户!',
                        type: 'error'
                    });
                    return;
                }
                if (!this.cheackIP(item.key, index)) {
                    this.$message({
                        message: 'IP地址不合法!',
                        type: 'error'
                    });
                    return;
                }
            }
            const h = this.$createElement;
            this.$msgbox({
                title: '消息',
                message: ((index == 1) || (index == 2)) ? h('p', null, [
                    h('span', null, "确认修改  "),
                    h('b', {style: 'color: teal'}, item.key + ": " + item.names),
                    h('span', null, '  吗?')
                ]) : h('p', null, [
                    h('span', null, "确认修改  "),
                    h('b', {style: 'color: teal'}, item.key + ": " + item.value),
                    h('span', null, '  吗?')
                ]),
                showCancelButton: true,
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                beforeClose: (action, instance, done) => {
                    if (action === 'confirm') {
                        instance.confirmButtonLoading = true;
                        instance.confirmButtonText = '执行中...';
                        setTimeout(() => {
                            done();
                            setTimeout(() => {
                                instance.confirmButtonLoading = false;
                            }, 300);
                            var oldKey;
                            switch (index) {
                                case 0:
                                    this.$http.post(api.mycat.properties.server.editMycatSystemConfig, {
                                        key: item.key,
                                        value: item.value
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '修改成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '修改失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getConfig()
                                    });
                                    break;
                                case 1:
                                    oldKey = this.objToArr(this.MycatFirewallConfig.whitehost)[i].key;
                                    this.$http.post(api.mycat.properties.server.editWhiteHostItem, {
                                        oldKey: oldKey,
                                        key: item.key,
                                        value: JSON.stringify(item.names)
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '修改成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '修改失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    break;
                                case 2:
                                    oldKey = this.objToArr(this.MycatFirewallConfig.whitehostMask)[i].key, 2;
                                    item = this.replaceIP(item, 1);
                                    this.$http.post(api.mycat.properties.server.editWhiteHostMaskItem, {
                                        oldKey: oldKey,
                                        key: item.key,
                                        value: JSON.stringify(item.names)
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '修改成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '修改失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    break;
                                case 3:
                                    this.$http.post(api.mycat.properties.server.editBlackItem, {
                                        key: item.key,
                                        value: item.value
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '修改成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '修改失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    break;
                            }
                        }, 3000);
                    } else {
                        done();
                    }
                }
            });
        },
        delItem(item, index) {
            const h = this.$createElement;
            this.$msgbox({
                title: '消息',
                message: h('p', null, [
                    h('span', null, "确认删除  "),
                    h('b', {style: 'color: teal'}, item.key),
                    h('span', null, '  吗?')
                ]),
                showCancelButton: true,
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                beforeClose: (action, instance, done) => {
                    if (action === 'confirm') {
                        instance.confirmButtonLoading = true;
                        instance.confirmButtonText = '执行中...';
                        setTimeout(() => {
                            done();
                            setTimeout(() => {
                                instance.confirmButtonLoading = false;
                            }, 300);
                            switch (index) {
                                case 1:
                                    this.$http.post(api.mycat.properties.server.deleWhiteHostItem, {
                                        key: item.key
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '删除成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '删除失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    break;
                                case 2:
                                    item = this.replaceIP(item, 1);
                                    this.$http.post(api.mycat.properties.server.deleWhiteHostMaskItem, {
                                        key: item.key
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '删除成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '删除失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    this.getFirewall();
                                    break;
                                case 3:
                                    this.$http.post(api.mycat.properties.server.deleBlackItem, {
                                        key: item.key,
                                        value: item.value
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '删除成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '删除失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                    });
                                    break;
                            }
                        }, 3000);
                    } else {
                        done();
                    }
                }
            });
        },
        addItem(index) {
            var key;
            if (index == 3) {
                if (this.addBlackItem.key == undefined) {
                    this.$message({
                        message: '请输入黑名单名称!',
                        type: 'error'
                    });
                    return;
                }
                if (!this.cheackIP(this.addBlackItem.value, index)) {
                    this.$message({
                        message: 'IP地址不合法!',
                        type: 'error'
                    });
                    return;
                }
            } else {
                if (index == 1) {
                    key = this.addWhiteHost;
                } else if (index == 2) {
                    key = this.addWhiteHostMask;
                }
                if (key.names == undefined || key.names.length <= 0) {
                    this.$message({
                        message: '请选择对应用户!',
                        type: 'error'
                    });
                    return;
                }
                if (!this.cheackIP(key.key, index)) {
                    this.$message({
                        message: 'IP地址不合法!',
                        type: 'error'
                    });
                    return;
                }
            }
            const h = this.$createElement;
            this.$msgbox({
                title: '消息',
                message: (index == 3) ? h('p', null, [
                    h('span', null, "确认添加  "),
                    h('b', {style: 'color: teal'}, this.addBlackItem.key + ": " + this.addBlackItem.value),
                    h('span', null, '  吗?')
                ]) : h('p', null, [
                    h('span', null, "确认添加  "),
                    h('b', {style: 'color: teal'}, key.key + ": " + key.names),
                    h('span', null, '  吗?')
                ]),
                showCancelButton: true,
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                beforeClose: (action, instance, done) => {
                    if (action === 'confirm') {
                        instance.confirmButtonLoading = true;
                        instance.confirmButtonText = '执行中...';
                        setTimeout(() => {
                            done();
                            setTimeout(() => {
                                instance.confirmButtonLoading = false;
                            }, 300);
                            switch (index) {
                                case 1:
                                    this.$http.post(api.mycat.properties.server.addWhiteHostItem, {
                                        key: key.key,
                                        value: JSON.stringify(key.names)
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '添加成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '添加失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                        this.addWhiteHost = {};
                                        this.isAddWhiteHost = false;
                                    });
                                    break;
                                case 2:
                                    key = this.replaceIP(key, 1);
                                    this.$http.post(api.mycat.properties.server.addWhiteHostMaskItem, {
                                        key: key.key,
                                        value: JSON.stringify(key.names)
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '添加成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '添加失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                        this.addWhiteHostMask = {};
                                        this.isAddWhiteHostMask = false;
                                    });
                                    break;
                                case 3:
                                    this.$http.post(api.mycat.properties.server.addBlackItem, {
                                        key: this.addBlackItem.key,
                                        value: this.addBlackItem.value
                                    }).then(res => {
                                        if (res.body.code == 200 && res.body.data) {
                                            this.$message({
                                                message: '添加成功!',
                                                type: 'success'
                                            });
                                        } else {
                                            this.$message({
                                                message: '添加失败!',
                                                type: 'error'
                                            });
                                        }
                                        this.getFirewall();
                                        this.addBlackItem = {};
                                        this.isAddBlackItem = false;
                                    });
                                    break;
                            }
                        }, 3000);
                    } else {
                        done();
                    }
                }
            });
        },
        getConfig() {
            this.$http.get(api.mycat.properties.server.getMycatSystemConfig).then(response => {
                this.MycatSystemConfig = this.filterSystem(response.body.data);
            });
        },
        getUsers() {
            this.$http.get(api.mycat.properties.server.getMycatUsersConfig).then(response => {
                let entries = Object.entries(response.body.data);
                for (let i = 0; i < entries.length; i++) {
                    if (entries[i] != undefined) {
                        this.users.push({
                            key: entries[i][0],
                            value: this.filterUser(entries[i][1])
                        });
                    }
                }
            });
        },
        getFirewall() {
            this.$http.get(api.mycat.properties.server.getMycatAllFirewallConfig).then(response => {
                this.MycatFirewallConfig = response.body.data;
                this.whiteHost = this.objToArr(this.MycatFirewallConfig.whitehost);
                this.whiteHostMask = this.objToArr(this.MycatFirewallConfig.whitehostMask);
                this.replaceIP(this.whiteHostMask);
                this.blackList = this.objToArr(this.MycatFirewallConfig.blacklist);
                this.loading = false;
            });
        },
        strFormat(value) {
            return value.replace(/[;:]/g, "<br/>");
        },
        objToArr(obj) {
            let entries = Object.entries(obj);
            let arr = [];
            for (let i = 0; i < entries.length; i++) {
                if (entries[i] != undefined) {
                    let names = [];
                    let item = entries[i][1];
                    if (item instanceof Array) {
                        for (let j = 0; j < item.length; j++) {
                            names.push(item[i].name)
                        }
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
                value: this.filterPrivilegesConfig(user.privilegesConfig)
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
                name: "是否只读",
                key: "readOnly",
                value: user.readOnly
            });
            u.push({
                name: "所管理的逻辑库",
                key: "schemas",
                value: user.schemas
            });
            return u;
        },
        filterPrivilegesConfig(privilegesConfig) {
            let schema = privilegesConfig.schemaPrivileges;
            let dataNode = privilegesConfig.dataNodePrivileges;
            this.filterDML(schema)
            this.filterDML(dataNode)
            for (let i = 0; i < schema.length; i++) {
                this.filterDML(schema[i].tablePrivileges)
            }
            return privilegesConfig
        },
        filterDML(schema) {
            let insert = [18, 23, 28, 14, 27, 29]
            let update = [30, 25, 13, 10, 29, 14]
            let select = [28, 14, 21, 14, 12, 29]
            let deleteChar = [13, 14, 21, 14, 29, 14]
            for (let i = 0; i < schema.length; i++) {
                schema[i].dmlName = ''
                if (schema[i].dml.length > 6) {
                    schema[i].dml = this.group(schema[i].dml, 6)
                    for (let j = 0; j < schema[i].dml.length; j++) {
                        switch (schema[i].dml[j].toString()) {
                            case select.toString():
                                schema[i].dmlName += "select "
                                break;
                            case insert.toString():
                                schema[i].dmlName += "insert "
                                break;
                            case update.toString():
                                schema[i].dmlName += "update "
                                break;
                            case deleteChar.toString():
                                schema[i].dmlName += "delete "
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    switch (schema[i].dml.toString()) {
                        case select.toString():
                            schema[i].dmlName += "select "
                            break;
                        case insert.toString():
                            schema[i].dmlName += "insert "
                            break;
                        case update.toString():
                            schema[i].dmlName += "update "
                            break;
                        case deleteChar.toString():
                            schema[i].dmlName += "delete "
                            break;
                        default:
                            break;
                    }
                }
            }
        },
        group(array, subGroupLength) {
            let index = 0;
            let newArray = [];
            while (index < array.length) {
                newArray.push(array.slice(index, index += subGroupLength));
                index+=1
            }
            return newArray;
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
