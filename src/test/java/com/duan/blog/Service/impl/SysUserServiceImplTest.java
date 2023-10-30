package com.duan.blog.Service.impl;

import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SysUserServiceImplTest {
    @Resource
    ISysUserService userService;

    @Test
    void login() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setAccount("admin");
        loginInfo.setPassword("admin");
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

    @Test
    void logout() {
    }

    @Test
    void getUserInfo() {
        System.out.println(userService.getUserInfo(1L));
    }

    @Test
    void updateUserInfo() {
        EditInfo editInfo = EditInfo.builder()
                .id(2L)
                .nickname("阿尔卡蒂奥")
                .build();
        userService.updateUserInfo(editInfo);
    }

    @Test
    void updatePassword() {
        System.out.println(userService.UpdatePassword(new ChangeInfo()
                .builder()
                .account("admin")
                .oldPassword("adminn")
                .newPassword("admin")
                .build()));
    }

    @Test
    void listFans() {
        System.out.println(userService.listFans());
    }
}