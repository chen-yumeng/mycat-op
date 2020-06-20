package io.mycat.web.controller;

import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @program: Mycat->LoginController
 * @description:
 * @author: cg
 * @create: 2020-06-20 10:49
 **/
@RestController
public class LoginController {

    @Value("${admin.username}")
    private String username;

    @Value("${admin.password}")
    private String password;

    @PostMapping("/mycat/login")
    public ResultCode login(String username, String password, HttpSession session) {
        try {
            if (this.username.equals(username)&&this.password.equals(password)) {
                session.setAttribute("user", username);
                return new ResultCode(200, "登录成功!");
            } else {
                return new ResultCode(500, "用户名或密码错误!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

}
