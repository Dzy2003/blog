package com.duan.blog.Service;

import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author soga
* @description 针对表【tb_follow】的数据库操作Service
* @createDate 2023-10-21 09:34:41
*/
public interface IFollowService extends IService<Follow> {

    Result isFollowUser(Long otherUserID);

    Result followUser(Long followUserId, Boolean isFollow);

    Result followCommons(Long uid);
}
