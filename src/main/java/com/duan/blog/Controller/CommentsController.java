package com.duan.blog.Controller;

import com.duan.blog.Service.ICommentsService;
import com.duan.blog.dto.CommentInfo;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.http.server.RequestPath;
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

    @GetMapping("/article/{id}")
    public Result listComments(@PathVariable("id") Long id){
        return commentsService.getCommentsByArticleId(id);
    }

    @PostMapping("/create/change")
    public Result comment(@RequestBody CommentInfo commentInfo){
        return commentsService.insertComment(commentInfo);
    }
}
