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
     *
     * @return 文章列表
     */
    Result listArticles(PageInfo pageInfo);

    /**
     * 获取最火文章列表
     */
    Result getHotArticles();

    /**
     * 获取最新文章列表
     */
    Result getNewArticles();

    /**
     * 获取文章归档
     * @return 年月和对应文章数
     */
    Result getArchives();

    /**
     * 根据ID获取文章详细信息
     * @param id 文章Id
     * @param isEdit 是否是编辑文章
     * @return 文章的详细信息
     */
    Result detailArticle(Long id,Boolean isEdit);

    /**
     * 更新或者插入文章(取决于articleInfo是否有id)
     * @param articleInfo 传入的文章信息
     * @return 成功或者失败
     */
    Result insertOrUpdateArticle(ArticleInfo articleInfo);

    /**
     * 用户点赞文章
     * @param id 文章id
     * @return 成功或者失败
     */
    Result likeArticle(Long id);

    /**
     * 获取文章的点赞用户（按照最新点赞排序）
     * @param id 文章id
     * @return 点赞用户
     */
    Result getArticleLikes(Long id);

    /**
     *滚动查询用户关注用户的博客
     * @param max 最大时间戳（上一页的最小时间戳）
     * @param offset 偏移量（第一页为0，之后为1）
     * @return 文章列表
     */
    Result getBlogOfFollow(Long max, Integer offset);

    /**
     * 删除文章
     * @param articleId 文章Id
     * @param authorId 作者Id
     * @return 成功或者失败
     */
    Result deleteArticleByAuthorId(Long articleId, Long authorId);

    /**
     * 置顶文章
     * @param id 文章id
     * @return null
     */
    Result topArticle(Long id);
}
