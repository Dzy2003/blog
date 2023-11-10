package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.CommentInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Comment;

public interface ICommentsService extends IService<Comment> {
    /**
     * 根据文章id获取该文章的所有评论
     * @param id
     * @param page
     * @param size
     * @return
     */
    Result getCommentsByArticleId(Long id, Integer page, Integer size);

    /**
     * 插入评论
     * @param commentInfo
     * @return
     */
    Result insertComment(CommentInfo commentInfo);

    /**
     * 获取该评论的所有回复
     * @param id 评论ID
     * @return 评论下的回复
     */
    Result getChildComments(Long id, Integer page, Integer size);

    /**
     * 当前登录用户给评论点赞
     * @param id 评论id
     * @return 成功和失败
     */
    Result likeComment(Long id);
}
