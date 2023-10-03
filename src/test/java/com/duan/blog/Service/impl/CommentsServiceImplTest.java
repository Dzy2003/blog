package com.duan.blog.Service.impl;

import com.duan.blog.Service.ICommentsService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CommentsServiceImplTest {
    @Resource
    ICommentsService commentsService;
    @Test
    public void getCommentsByArticleId() throws Exception {
        System.out.println(commentsService.getCommentsByArticleId(1l));
    }

    @Test
    void insertComment() {

    }
}