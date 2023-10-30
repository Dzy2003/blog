package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.SysUserMapper;
import com.duan.blog.Service.IFollowService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.aop.annotation.LogAnnotation;
import com.duan.blog.dto.*;
import com.duan.blog.pojo.Follow;
import com.duan.blog.pojo.SysUser;
import com.duan.blog.utils.JWTUtils;
import com.duan.blog.utils.UserHolder;
import com.duan.blog.vo.UserInfoVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static com.duan.blog.utils.ErrorCode.*;
import static com.duan.blog.utils.SystemConstants.SLAT;
//TODO:在登录相关功能中加入redis

@Service
@Slf4j
 public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService{
    @Resource
    @Lazy
    IFollowService followService;

    @Override
    public Result login(LoginInfo loginInfo) {
        if(loginInfo == null || StrUtil.isBlank(loginInfo.getAccount()) || StrUtil.isBlank(loginInfo.getPassword())){
            return Result.fail(ACCOUNT_PWD_NOT_INPUT.getCode(),ACCOUNT_PWD_NOT_INPUT.getMsg());
        }
        SysUser loginUser = lambdaQuery().select(SysUser::getId,SysUser::getPassword,SysUser::getSalt)
                .eq(SysUser::getAccount, loginInfo.getAccount())
                .one();

        if(loginUser == null || !checkPassword(loginInfo.getPassword(),loginUser.getPassword(),loginUser.getSalt())){
            return Result.fail(ACCOUNT_PWD_NOT_EXIST.getCode(),ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        lambdaUpdate()
                .set(SysUser::getLastLogin, System.currentTimeMillis())
                .eq(SysUser::getId, loginUser.getId())
                .update();

        String token = JWTUtils.createToken(loginUser.getId());

        // TODO:生成token后将该token存入redis中

        return Result.success(token);

    }

    private Boolean checkPassword(String InputPassword, String truePassword, String salt){
        return DigestUtils.md5Hex(InputPassword + salt).equals(truePassword);
    }

    @Override
    public Result getCurrentUser(String token) {
        if(token == null) return Result.fail(NO_LOGIN.getCode(),NO_LOGIN.getMsg());

        Map<String, Object> checkMsg = JWTUtils.checkToken(token);
        return Result.success(getUserDTO(Long.valueOf(checkMsg.get("userId").toString())));
    }

    private UserDTO getUserDTO(Long userId) {
        return BeanUtil.copyProperties(lambdaQuery()
                .select(SysUser::getId,
                        SysUser::getAccount,
                        SysUser::getNickname,
                        SysUser::getAvatar)
                .eq(SysUser::getId, userId)
                .one(), UserDTO.class);
    }

    @Override
    public Result logout(String token) {
        //TODO:将存放在redis中的token删除

        return Result.success(null);
    }

    @Override
    @Transactional
    public Result register(RegisterInfo registerInfo) {
        if(registerInfo == null || StrUtil.isBlank(registerInfo.getAccount())
                || StrUtil.isBlank(registerInfo.getPassword())
                || StrUtil.isBlank(registerInfo.getNickname())){
            return Result.fail(ACCOUNT_PWD_NOT_INPUT.getCode(),ACCOUNT_PWD_NOT_INPUT.getMsg());
        }

        Long count = lambdaQuery()
                .eq(SysUser::getAccount, registerInfo.getAccount())
                .last("limit 1")
                .count();

        if(count > 0) return Result.fail(ACCOUNT_EXIST.getCode(),ACCOUNT_EXIST.getMsg());

        Long newUserID = insertRegisterUser(registerInfo);

        log.info("newUserID:" + newUserID);
        return Result.success(JWTUtils.createToken(newUserID));
    }

    @Override
    @LogAnnotation(module = "userService", operator = "获取用户详情")
    public Result getUserInfo(Long id) {
        return Result.success(
                BeanUtil.copyProperties(
                lambdaQuery()
                        .eq(SysUser::getId, id)
                        .select(SysUser::getId,SysUser::getAccount,SysUser::getAvatar,SysUser::getCreateDate,
                        SysUser::getNickname,SysUser::getLastLogin,SysUser::getEmail,SysUser::getMobilePhoneNumber)
                        .one()
        ,UserInfoVo.class));
    }

    @Override
    public Result updateUserInfo(EditInfo editInfo) {
        lambdaUpdate()
                .set(StrUtil.isNotEmpty(editInfo.getNickname()),SysUser::getNickname,editInfo.getNickname())
                .set(StrUtil.isNotEmpty(editInfo.getEmail()),SysUser::getEmail,editInfo.getEmail())
                .set(StrUtil.isNotEmpty(editInfo.getMobilePhoneNumber())
                        ,SysUser::getMobilePhoneNumber,editInfo.getMobilePhoneNumber())
                .eq(SysUser::getId, editInfo.getId())
                .update();
        return Result.success(null);

    }


    @Override
    public Result UpdatePassword(ChangeInfo changeInfo) {
        if(changeInfo == null
                || StrUtil.isBlank(changeInfo.getAccount())
                || StrUtil.isBlank(changeInfo.getNewPassword())
                || StrUtil.isBlank(changeInfo.getOldPassword())){
            return Result.fail(ACCOUNT_PWD_NOT_INPUT.getCode(),ACCOUNT_PWD_NOT_INPUT.getMsg());
        }
        SysUser user = lambdaQuery().select(SysUser::getId,SysUser::getPassword,SysUser::getSalt)
                .eq(SysUser::getAccount, changeInfo.getAccount())
                .one();
        if(user == null || !checkPassword(changeInfo.getOldPassword(),user.getPassword(),user.getSalt())){
            return Result.fail(ACCOUNT_PWD_NOT_EXIST.getCode(),ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        lambdaUpdate()
                .set(SysUser::getPassword, DigestUtils.md5Hex(changeInfo.getNewPassword()+SLAT))
                .eq(SysUser::getId, user.getId())
                .update();
        return Result.success(null);

    }

    @Override
    public Result listFans() {
        return Result.success(
                followService.lambdaQuery()
                        .select(Follow::getUserId)
                        .eq(Follow::getFollowUserId, UserHolder.getUser().getId())
                        .list()
                        .stream()
                        .map(Follow::getUserId)
                        .map(this::getUserDTO)
                        .collect(Collectors.toList())
        );
    }


    private Long insertRegisterUser(RegisterInfo registerInfo) {
        SysUser newUser = new SysUser();
        newUser.setNickname(registerInfo.getNickname());
        newUser.setAccount(registerInfo.getAccount());
        newUser.setPassword(DigestUtils.md5Hex(registerInfo.getPassword()+SLAT));
        newUser.setCreateDate(System.currentTimeMillis());
        newUser.setLastLogin(System.currentTimeMillis());
        newUser.setAvatar("/static/img/logo.b3a48c0.png");
        newUser.setAdmin(1); //1 为true
        newUser.setDeleted(0); // 0 为false
        newUser.setSalt(SLAT);
        newUser.setStatus("");
        newUser.setEmail("");
        save(newUser);
        return newUser.getId();
    }
}
