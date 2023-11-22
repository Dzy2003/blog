package com.duan.blog.Controller;

import com.duan.blog.Service.IArticleService;
import com.duan.blog.dto.ArticleInfo;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    public IArticleService articleService;
    @Autowired
    public ArticleController(IArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 展示文章列表
     * @param pageInfo 查询文章列表的一些条件
     * @return 文章的集合
     */
    @PostMapping("")
    public Result listArticles(@RequestBody PageInfo pageInfo){
        return articleService.listArticles(pageInfo);
    }

    /**
     * 展示最火文章
     * @return 文章集合
     */
    @PostMapping("/hot")
    public Result listHotArticles(){
        return articleService.getHotArticles();
    }

    /**
     * 展示最新文章
     * @return 文章集合
     */
    @PostMapping("/new")
    public Result listNewArticles(){
        return articleService.getNewArticles();
    }

    /**
     * 获取文章归档（根据年月分类文章）
     * @return 年月和对应文章数
     */
    @PostMapping("/listArchives")
    public Result listArchives(){
        return articleService.getArchives();
    }

    /**
     * 获取文章详细信息
     * @param id 文章id
     * @return 文章详细信息
     */
    @PostMapping("/view/{id}")
    public Result detailArticle(@PathVariable("id") Long id){
        return articleService.detailArticle(id,false);
    }

    /**
     * 发表文章
     * @param articleInfo 文章信息
     * @return 成功或失败
     */
    @PostMapping("/publish")
    public Result publishArticle(@RequestBody ArticleInfo articleInfo){
        return articleService.insertOrUpdateArticle(articleInfo);
    }

    /**
     * 编辑文章
     * @param id 文章id
     * @return 成功或者失败
     */
    @PostMapping("/{id}")
    public Result EditArticle(@PathVariable("id") Long id){
        return articleService.detailArticle(id,true);
    }

    /**
     * 用户点赞文章
     * @param id 文章id
     * @return 成功或者失败
     */
    @PutMapping("/likes/{id}")
    public Result likeArticle(@PathVariable("id") Long id){
        return articleService.likeArticle(id);
    }

    /**
     * 获取文章点赞排行榜（时间排序）
     * @param id 文章id
     * @return null
     */
    @GetMapping("/likeUsers/{id}")
    public Result queryArticleLikes(@PathVariable("id") Long id){
        return articleService.getArticleLikes(id);
    }

    /**
     * 滚动查询用户关注用户的博客
     * @param max 最大时间戳（第一次为当前时间戳，之后为上一次查询的最小时间戳）
     * @param offset 偏移量（第一次为0，之后为1）
     * @return 当页文章列表
     */
    @GetMapping("of/follow")
    public Result listBlogOfFollow(@RequestParam("lastID") Long max,
                                   @RequestParam(value = "offset",defaultValue = "0") Integer offset){
        return articleService.getBlogOfFollow(max,offset);
    }

    /**
     * 删除文章
     * @param articleId 文章id
     * @param authorId 作者id
     * @return 文章作者与登录用户相同则删除,否则失败
     */
    @DeleteMapping("/{articleId}/{authorId}")
    public Result deleteArticleByAuthorId(@PathVariable("articleId") Long articleId,@PathVariable Long authorId){
        return articleService.deleteArticleByAuthorId(articleId,authorId);
    }

    /**
     * 设置文章置顶
     * @param id 文章ID
     * @return null
     */
    @PutMapping("/top/{id}")
    public Result topArticle(@PathVariable("id") Long id){
        return articleService.topArticle(id);
    }
}
