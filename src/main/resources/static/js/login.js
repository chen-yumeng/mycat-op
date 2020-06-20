function login() {
    var username = document.getElementById("username");
    var password = document.getElementById("password");
    if (app.check(username.value, password.value)) {
        return
    }
    addClass(document.querySelector(".login"), "active");
    setTimeout(function () {
        addClass(document.querySelector(".sk-rotating-plane"), "active");
        document.querySelector(".login").style.display = "none";
        app.login(username.value, password.value);
    }, 800);
    setTimeout(function () {
        removeClass(document.querySelector(".login"), "active");
        removeClass(document.querySelector(".sk-rotating-plane"), "active");
        document.querySelector(".login").style.display = "block";
    }, 3000);
}

function hasClass(elem, cls) {
    cls = cls || '';
    if (cls.replace(/\s/g, '').length == 0) return false; //当cls没有参数时，返回false
    return new RegExp(' ' + cls + ' ').test(' ' + elem.className + ' ');
}

function addClass(ele, cls) {
    if (!hasClass(ele, cls)) {
        ele.className = ele.className == '' ? cls : ele.className + ' ' + cls;
    }
}

function removeClass(ele, cls) {
    if (hasClass(ele, cls)) {
        var newClass = ' ' + ele.className.replace(/[\t\r\n]/g, '') + ' ';
        while (newClass.indexOf(' ' + cls + ' ') >= 0) {
            newClass = newClass.replace(' ' + cls + ' ', ' ');
        }
        ele.className = newClass.replace(/^\s+|\s+$/g, '');
    }
}

var app = new Vue({
    el: '#app',
    data: {
        defaultActive: 'Mycat监控系统'
    },
    methods: {
        check(username, password) {
            if (username == '' || password == '') {
                this.$message({
                    message: "用户名或密码不能为空！",
                    type: 'error'
                });
                return true;
            }
            return false;
        },
        login(username, password) {
            app.$http.post(api.mycat.login.login, {
                username: username,
                password: password
            }).then(response => {
                if (response.body.code == 200) {
                    window.location.href = "/";
                } else {
                    this.$message({
                        message: response.body.data,
                        type: 'error'
                    });
                }
            });
        },
    },
});


