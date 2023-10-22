package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.ArticleInfo;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Article;

public interface IArticleService extends IService<Article> {
    /**
     * 展示文章列表，通过pageInfo参数动态查询
     * @param pageInfo
     * 1.仅传page和pageSize则是首页查询
     * 2.传入categoryId则是分类查询
     * 3.传入year和mouth则是归档查询
     * 4.传入authorId则是用户文章查询
     * 动态sql保证传入多少参数就有多少查询条件
     * @return
     */
    Result listArticles(PageInfo pageInfo);

    /**
     * 获取最火文章列表
     * @return
     */
    Result getHotArticles();

    /**
     * 获取最新文章列表
     * @return
     */
    Result getNewArticles();

    /**
     * 获取文章归档
     * @return
     */
    Result getArchives();

    /**
     * 根据ID获取文章详细信息
     * @param id
     * @param isEdit
     * @return
     */
    Result detailArticle(Long id,Boolean isEdit);

    /**
     * 更新或者插入文章(取决于articleInfo是否有id)
     * @param articleInfo
     * @return
     */
    Result insertOrUpdateArticle(ArticleInfo articleInfo);

    /**
     * 用户点赞文章
     * @param id
     * @return
     */
    Result likeArticle(Long id);

    /**
     * 获取文章的点赞用户（按照最新点赞排序）
     * @param id
     * @return
     */
    Result getArticleLikes(Long id);
}
