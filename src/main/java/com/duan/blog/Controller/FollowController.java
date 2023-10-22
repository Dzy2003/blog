package com.duan.blog.Controller;

import com.duan.blog.Service.IFollowService;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.ResultMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author 白日
 * @date Created in 2023/10/21 9:38
 */
@RestController
@RequestMapping("/follows")
public class FollowController {
    @Resource
    IFollowService service;

    /**
     * 关注
     * @param followUserId 关注的用户id
     * @param isFollow 关注或者取关
     * @return
     */
    @PutMapping("/{uid}/{isFollow}")
    public Result followUser(@PathVariable("uid") Long followUserId, @PathVariable Boolean isFollow){
        return service.followUser(followUserId,isFollow);
    }

    /**
     * 当前用户是否关注该用户
     * @param uid 该用户id
     * @return
     */
    @GetMapping("/{uid}")
    public Result isFollowUser(@PathVariable("uid") Long uid){
        return service.isFollowUser(uid);
    }

    /**
     * 获取当前用户和该用户的共同关注
     * @param uid 该用户id
     * @return
     */
    @GetMapping("/common/{uid}")
    public Result getCommonUsers(@PathVariable("uid") Long uid){
        return service.followCommons(uid);
    }


}
