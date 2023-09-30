package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.SysUserMapper;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.LoginInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.dto.UserDto;
import com.duan.blog.entity.SysUser;
import com.duan.blog.utils.ErrorCode;
import com.duan.blog.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.duan.blog.utils.ErrorCode.*;
import static com.duan.blog.utils.SystemConstants.SLAT;
//TODO:在登录相关功能中加入redis

@Service
@Slf4j
 public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService{
    @Override
    public Result login(LoginInfo loginInfo) {
        if(loginInfo == null || StrUtil.isBlank(loginInfo.getAccount()) || StrUtil.isBlank(loginInfo.getPassword())){
            return Result.fail(ACCOUNT_PWD_NOT_INPUT.getCode(),ACCOUNT_PWD_NOT_INPUT.getMsg());
        }

        SysUser user = lambdaQuery().select(SysUser::getId,SysUser::getPassword)
                .eq(SysUser::getAccount, loginInfo.getAccount())
                .one();

        if(user == null) return Result.fail(ACCOUNT_PWD_NOT_EXIST.getCode(),ACCOUNT_PWD_NOT_EXIST.getMsg());

        //log.info("userId:" + user.getId());

        if(! DigestUtils.md5Hex(loginInfo.getPassword() + SLAT) .equals(user.getPassword())){
            return Result.fail(ACCOUNT_PWD_NOT_EXIST.getCode(),ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        String token = JWTUtils.createToken(user.getId());

        // TODO:生成token后将该token存入redis中

        return Result.success(token);

    }

    @Override
    public Result getCurrentUser(String token) {
        if(token == null) return Result.fail(NO_LOGIN.getCode(),NO_LOGIN.getMsg());

        Map<String, Object> checkMsg = JWTUtils.checkToken(token);

        Long userId = Long.valueOf(checkMsg.get("userId").toString());

        return Result.success(
                BeanUtil.copyProperties(lambdaQuery()
                .select(SysUser::getId,
                        SysUser::getAccount,
                        SysUser::getNickname,
                        SysUser::getAvatar)
                .eq(SysUser::getId, userId)
                .one(), UserDto.class));
    }

    @Override
    public Result logout(String token) {
        //TODO:将存放在redis中的token删除
        return Result.success(null);
    }
}
