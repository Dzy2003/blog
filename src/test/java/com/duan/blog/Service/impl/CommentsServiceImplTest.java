package com.duan.blog.Service.impl;

import com.duan.blog.Service.ICommentsService;
import com.duan.blog.pojo.Comment;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.GsonTester;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CommentsServiceImplTest {
    @Resource
    ICommentsService commentsService;
    @Test
    public void getCommentsByArticleId() throws Exception {
        System.out.println(commentsService.getCommentsByArticleId(10L));
    }


    @Test
    void insertComment() {

    }
}