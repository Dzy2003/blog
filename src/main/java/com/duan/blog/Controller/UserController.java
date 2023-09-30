package com.duan.blog.Controller;

import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.LoginInfo;
import com.duan.blog.dto.RegisterInfo;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    ISysUserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginInfo loginInfo){
        return userService.login(loginInfo);
    }

    @GetMapping("/currentUser")
    public Result currentUser(@RequestHeader("Authorization") String token){
        return userService.getCurrentUser(token);
    }

    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        return userService.logout(token);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterInfo registerInfo){
        return userService.register(registerInfo);
    }

}
