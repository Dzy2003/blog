package com.duan.blog.Controller;

import com.duan.blog.Service.ICommentsService;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.http.server.RequestPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
