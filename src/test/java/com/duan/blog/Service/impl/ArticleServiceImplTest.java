package com.duan.blog.Service.impl;

import com.duan.blog.Service.IArticleService;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class ArticleServiceImplTest {
    @Autowired
    IArticleService articleService;
    @Test
    public void testListArticle(){
        Result result = articleService.listArticles(new PageInfo(1, 3));
        log.info(result.toString());
    }
}