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
}
