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
    /**
     * 判断是否关注
     * @param otherUserID 其它用户
     * @return 是否关注
     */
    Result isFollowUser(Long otherUserID);
    /**
     * 关注用户
     * @param followUserId 关注用户ID
     * @param isFollow 是否关注
     * @return 关注结果
     */
    Result followUser(Long followUserId, Boolean isFollow);

    /**
     * 关注公共
     * @param uid 用户ID
     * @return 关注结果
     */
    Result followCommons(Long uid);
}
