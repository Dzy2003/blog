package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Service.IFollowService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.Result;
import com.duan.blog.dto.UserDTO;
import com.duan.blog.pojo.Follow;
import com.duan.blog.mapper.FollowMapper;
import com.duan.blog.pojo.SysUser;
import com.duan.blog.utils.RedisConstants;
import com.duan.blog.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
* @author soga
* @description 针对表【tb_follow】的数据库操作Service实现
* @createDate 2023-10-21 09:34:41
*/
@Service
public class IFollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    ISysUserService userService;

    @Override
    public Result isFollowUser(Long otherUserID) {
        return Result.success(getIsFollow(otherUserID));
    }

    private Boolean getIsFollow(Long otherUserID) {
        Boolean isFollow = stringRedisTemplate.opsForSet().isMember(
                RedisConstants.FOLLOWED_KEY + UserHolder.getUser().getId().toString(),
                otherUserID.toString());
        return isFollow;
    }

    @Override
    public Result followUser(Long followUserId, Boolean isFollow) {
        if(isFollow){
            Follow(followUserId);
        }else{
            cancelFollow(followUserId);
        }
        return Result.success(null);
    }

    /**
     * 取消关注
     * @param followUserId 取关id
     */
    private void cancelFollow(Long followUserId) {
        boolean success = lambdaUpdate().eq(Follow::getUserId, UserHolder.getUser().getId())
                .eq(Follow::getFollowUserId, followUserId)
                .remove();
        if(success) {
            stringRedisTemplate.opsForSet().remove(
                    RedisConstants.FOLLOWED_KEY + UserHolder.getUser().getId().toString(),
                    followUserId);
        }
    }

    /**
     * 关注
     * @param followUserId 关注id
     */
    private void Follow(Long followUserId) {
        savaFollow(followUserId);
        stringRedisTemplate.opsForSet().add(RedisConstants.FOLLOWED_KEY + UserHolder.getUser().getId()
                , followUserId.toString());
    }

    private void savaFollow(Long followUserId) {
        Follow follow = new Follow();
        follow.setUserId(UserHolder.getUser().getId());
        follow.setFollowUserId(followUserId);
        follow.setCreateTime(LocalDateTime.now());
        save(follow);
    }

    @Override
    public Result followCommons(Long uid) {
        return Result.success(
                stringRedisTemplate.opsForSet().intersect(
                RedisConstants.FOLLOWED_KEY + UserHolder.getUser().getId(),
                RedisConstants.FOLLOWED_KEY + uid).stream()
                .map(Long::valueOf)
                .map(this::getUserDTOById)
                .collect(Collectors.toList()));
    }

    /**
     * 通过uid获得UserDTO
     * @param id 用户id
     * @return
     */
    private UserDTO getUserDTOById(Long id){
        return BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAccount, SysUser::getNickname, SysUser::getAvatar)
                .eq(SysUser::getId, id)
                .one(), UserDTO.class);
    }
}




