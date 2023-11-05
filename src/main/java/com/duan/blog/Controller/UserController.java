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

    /**
     * 登录
     * @param loginInfo 登录信息
     * @return token
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginInfo loginInfo){
        return userService.login(loginInfo);
    }

    /**
     * 获取当前用户信息
     * @param token token
     * @return 用户信息
     */
    @GetMapping("/currentUser")
    public Result currentUser(@RequestHeader("Authorization") String token){
        return userService.getCurrentUser(token);
    }

    /**
     * 退出登录
     * @param token 凭证
     * @return 成功失败
     */
    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        return userService.logout(token);
    }

    /**
     * 注册
     * @param registerInfo 注册信息
     * @return token
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterInfo registerInfo){
        return userService.register(registerInfo);
    }
    /**
     * 修改密码
     * 请求路径：/change_password
     * 请求方法：PUT
     * 请求参数：ChangeInfo changeInfo
     *
     * @param changeInfo 修改密码信息
     * @return Result 修改结果
     */
    @PutMapping("/change_password")
    public Result changePassword(@RequestBody ChangeInfo changeInfo){
        return userService.UpdatePassword(changeInfo);
    }
    /**
     * 获取用户详细信息
     *
     * @param id 用户ID
     * @return 返回用户信息Result对象
     */
    @GetMapping("/info/{id}")
    public Result UserDetail(@PathVariable("id") Long id){
        return userService.getUserInfo(id);
    }
    /**
     * 编辑用户信息
     *
     * @param editInfo 修改信息实体对象
     * @return 结果对象
     */
    @PutMapping("/info")
    public Result editInfo(@RequestBody EditInfo editInfo ){
        return userService.updateUserInfo(editInfo);
    }
    /**
     * 获取粉丝列表
     *
     * @return 返回粉丝列表结果对象
     */
    @GetMapping("/fans")
    public Result fans(){
        return userService.listFans();
    }



}
