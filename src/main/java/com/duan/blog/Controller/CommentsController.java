package com.duan.blog.Controller;

import com.duan.blog.Service.ICommentsService;
import com.duan.blog.dto.CommentInfo;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author 白日
 * @date Created in 2023/10/2 9:49
 */
@RestController
@RequestMapping("/comments")
public class CommentsController {
    @Resource
    ICommentsService commentsService;

    /**
     * 获取文章下的评论
     * @param id 文章id
     * @return 文章vo列表
     */
    @GetMapping("/article/{id}")
    public Result listComments(@PathVariable("id") Long id,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "5") Integer size){
        return commentsService.getCommentsByArticleId(id, page, size);
    }

    /**
     * 文章评论
     * @param commentInfo 评论信息CommentInfo
     * @return 评论成功失败
     */
    @PostMapping("/create/change")
    public Result comment(@RequestBody CommentInfo commentInfo){
        return commentsService.insertComment(commentInfo);
    }

    /**
     * 查询评论回复
     * @param id 评论Id
     * @return 评论回复列表
     */
    @GetMapping("/{id}")
    public Result getReply(@PathVariable("id") Long id,
                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "5") Integer size){
        return commentsService.getChildComments(id, page, size);
    }

}
