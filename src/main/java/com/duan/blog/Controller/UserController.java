package com.duan.blog.Controller;

import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.*;
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

    @PutMapping("/change_password")
    public Result changePassword(@RequestBody ChangeInfo changeInfo){
        return userService.UpdatePassword(changeInfo);
    }
    @GetMapping("/info/{id}")
    public Result UserDetail(@PathVariable("id") Long id){
        return userService.getUserInfo(id);
    }

    @PutMapping("/info")
    public Result editInfo(@RequestBody EditInfo editInfo ){
        return userService.updateUserInfo(editInfo);
    }

    @GetMapping("/fans")
    public Result fans(){
        return userService.listFans();
    }



}
