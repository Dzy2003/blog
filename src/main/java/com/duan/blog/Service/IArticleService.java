package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.ArticleInfo;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Article;

public interface IArticleService extends IService<Article> {

    Result listArticles(PageInfo pageInfo);

    Result getHotArticles();

    Result getNewArticles();

    Result getArchives();

    Result detailArticle(Long id);

    Result insertArticle(ArticleInfo articleInfo);
}
