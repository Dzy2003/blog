package com.duan.blog.Service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Mapper.SysUserMapper;
import com.duan.blog.Service.IArticleService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.aop.annotation.LogAnnotation;
import com.duan.blog.dto.*;
import com.duan.blog.pojo.Article;
import com.duan.blog.pojo.Follow;
import com.duan.blog.pojo.SysUser;
import com.duan.blog.utils.JWTUtils;
import com.duan.blog.vo.UserInfoVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import com.duan.blog.mapper.FollowMapper;
import static com.duan.blog.utils.ErrorCode.*;
import static com.duan.blog.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.duan.blog.utils.RedisConstants.FOLLOWED_KEY;
import static com.duan.blog.utils.SystemConstants.SLAT;
//TODO:在登录相关功能中加入redis

@Service
@Slf4j
 public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService{
    @Resource
    FollowMapper followMapper;
    @Resource
    ArticleMapper articleMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
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
        assert checkMsg != null;
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
        UserInfoVo userInfoVo = getBasicInfo(id);
        if(userInfoVo == null) return Result.fail(USER_NOT_EXIST.getCode(),USER_NOT_EXIST.getMsg());
        userInfoVo.setFollowCount(getFollowCount(id));
        userInfoVo.setFansCount(getFansCount(id));
        userInfoVo.setLikeCount(getLikeCount(id));
        userInfoVo.setArticleCount(getArticleCount(id));
        return Result.success(userInfoVo);
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
    public Result listUserArticle(Long uid) {
        return null;
    }

    /**
     * 获取点赞数量
     * @param id 用户id
     * @return 点赞数量
     */
    private Long getLikeCount(Long id) {
        LambdaQueryWrapper<Article> lqw = new LambdaQueryWrapper<>();
        List<Long> articleIdList = articleMapper
                .selectList(
                        lqw
                        .select(Article::getId)
                        .eq(Article::getAuthorId, id))
                .stream()
                .map(Article::getId)
                .toList();
        Long likeCount = 0L;
        for (Long articleId : articleIdList) {
            likeCount += stringRedisTemplate.opsForZSet()
                    .count(BLOG_LIKED_KEY + articleId,0,System.currentTimeMillis());
        }
        return likeCount;
    }

    /**
     * 获取用户文章数量
     * @param id 用户id
     * @return 文章数量
     */
    private Long getArticleCount(Long id) {
        LambdaQueryWrapper<Article> lqw = new LambdaQueryWrapper<>();
        return articleMapper.selectCount(lqw
                .eq(Article::getAuthorId, id)
                );
    }

    /**
     * 获取粉丝数量
     * @param id 用户id
     * @return 粉丝数量
     */
    private Long getFansCount(Long id) {
        LambdaQueryWrapper<Follow> lqw = new LambdaQueryWrapper<>();
        return followMapper.selectCount(lqw.eq(Follow::getFollowUserId,id));
    }

    /**
     * 获取关注数量
     * @param id 用户id
     * @return 关注数量
     */
    private Long getFollowCount(Long id) {
        return stringRedisTemplate.opsForSet().size(FOLLOWED_KEY + id);
    }

    /**
     * 从数据库获取用户基础信息
     * @param id 用户id
     * @return 基础信息
     */
    private UserInfoVo getBasicInfo(Long id) {
        SysUser sysUser = lambdaQuery()
                .eq(SysUser::getId, id)
                .select(SysUser::getId, SysUser::getAccount, SysUser::getAvatar, SysUser::getCreateDate,
                        SysUser::getNickname, SysUser::getLastLogin, SysUser::getEmail, SysUser::getMobilePhoneNumber)
                .one();
        if(BeanUtil.isEmpty(sysUser)) return null;
        return BeanUtil.copyProperties(sysUser, UserInfoVo.class);

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
