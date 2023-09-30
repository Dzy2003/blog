package com.duan.blog.Service.impl;

import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.LoginInfo;
import com.duan.blog.dto.RegisterInfo;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SysUserServiceImplTest {
    @Resource
    ISysUserService userService;

    @Test
    void login() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setAccount("admin1");
        loginInfo.setPassword("admin1");
        Result login = userService.login(loginInfo);
        System.out.println(login);
    }

    @Test
    void getCurrentUser() {
        String token1 ="eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2OTY5Mjg2MTEsInVzZXJJZCI6MSwiaWF0IjoxNjk2MDM5NTc4fQ.wxYS3fPiW1hmiPumwsDCq_CqIsuk_Znstfm0nHnnEVY";
        String token2 ="eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2OTY5MjgwMjEsInVzZXJJZCI6bnVsbCwiaWF0IjoxNjk2MDM4OTg5fQ.4tRcLDJaHis5n6qVqYKCyqsyyGlvM-GUMh0we3jYrMI";
        Result currentUser = userService.getCurrentUser(token1);
        System.out.println(currentUser);
    }

    @Test
    void register() {
        RegisterInfo registerInfo = new RegisterInfo();
        registerInfo.setAccount("admin1");
        registerInfo.setPassword("admin1");
        registerInfo.setNickname("阿尔卡蒂奥");
        Result register = userService.register(registerInfo);
        System.out.println(register);
    }
}