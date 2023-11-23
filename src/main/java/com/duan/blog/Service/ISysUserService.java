package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.*;
import com.duan.blog.pojo.SysUser;

public interface ISysUserService extends IService<SysUser> {
    /**
     * 登录
     * @param loginInfo 登录信息
     * @return 登录结果
     */
    Result login(LoginInfo loginInfo);

    /**
     * 获取当前用户信息
     * @param token token
     * @return 用户信息
     */
    Result getCurrentUser(String token);

    /**
     * 退出登录
     * @param token token
     * @return 退出结果
     */
    Result logout(String token);

    /**
     * 注册
     * @param registerInfo 注册信息
     * @return 注册结果
     */
    Result register(RegisterInfo registerInfo);

    /**
     * 获取用户
     * @param id 用户id
     * @return 用户信息
     */
    Result getUserInfo(Long id);

    /**
     * 更新用户信息
     * @param editInfo 编辑信息
     * @return 更新结果
     */
    Result updateUserInfo(EditInfo editInfo);

    /**
     * 修改密码
     * @param changeInfo 修改信息
     * @return 修改结果
     */
    Result UpdatePassword(ChangeInfo changeInfo);

    Result listUserArticle(Long uid);
}
