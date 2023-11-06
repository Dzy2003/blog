package com.duan.blog.Service.impl;

import com.duan.blog.Service.IArticleService;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.utils.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Slf4j
class ArticleServiceImplTest {
    @Autowired
    IArticleService articleService;
    @Test
    public void testListArticle(){
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(1);
        pageInfo.setPageSize(4);
//        pageInfo.setCategoryId(3l);
//        pageInfo.setTagId(8l);
        pageInfo.setAuthorId(1l);
        System.out.println(articleService.listArticles(pageInfo));
    }

    @Test
    void getHotArticles() {
        Result hotArticles = articleService.getHotArticles();
        log.info(hotArticles.toString());
    }

    @Test
    void getNewArticles() {
        Result newArticles = articleService.getNewArticles();
        log.info(newArticles.toString());
    }

    @Test
    void getArchives() {
        Result archives = articleService.getArchives();
        log.info(archives.toString());
    }

    @Test
    void detailArticle() {
        Result result = articleService.detailArticle(1L,false);
        System.out.println(result);
    }

    @Test
    void listArticles() {
    }

    @Test
    void insertOrUpdateArticle() {
    }

    @Test
    void likeArticle() {
    }

    @Test
    void getArticleLikes() {
        System.out.println(articleService.getArticleLikes(2L));
    }

    @Test
    void getBlogOfFollow() {
    }

    @Test
    void deleteArticleByAuthorId() {
        // Arrange
        Long articleId = 1405916999732707340L;
        Long authorId = 2L;

        // Act
        Result result = articleService.deleteArticleByAuthorId(articleId, authorId);

        System.out.println(result);
    }
}