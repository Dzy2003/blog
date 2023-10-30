package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.*;
import com.duan.blog.pojo.SysUser;

public interface ISysUserService extends IService<SysUser> {

    Result login(LoginInfo loginInfo);

    Result getCurrentUser(String token);

    Result logout(String token);

    Result register(RegisterInfo registerInfo);

    Result getUserInfo(Long id);

    Result updateUserInfo(EditInfo editInfo);

    Result UpdatePassword(ChangeInfo changeInfo);

    Result listFans();
}
