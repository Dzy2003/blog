package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Service.*;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.entity.Article;
import com.duan.blog.entity.ArticleBody;
import com.duan.blog.entity.Category;
import com.duan.blog.entity.SysUser;
import com.duan.blog.vo.ArticleBodyVo;
import com.duan.blog.vo.ArticleHotAndNewVo;
import com.duan.blog.vo.ArticleVo;
import com.duan.blog.vo.CategoryVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.duan.blog.utils.ErrorCode.ARTICLE_NOT_EXIST;
import static com.duan.blog.utils.SystemConstants.*;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    @Resource
    ITagService tagService;
    @Resource
    ISysUserService userService;
    @Resource
    ArticleMapper articleMapper;
    @Resource
    IArticleBodyService articleBodyService;
    @Resource
    ICategoryService categoryService;
    @Resource
    ThreadService threadService;

    @Override
    public Result listArticles(PageInfo pageInfo) {
        if(pageInfo == null) return Result.fail(1,"No page info");
        List<Article> records = lambdaQuery().orderByDesc(Article::getWeight)
                .orderByDesc(Article::getCreateDate)
                .page(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()))
                .getRecords();
        //log.info("数据库查询数据：" + records.toString());
        return Result.success(ArticleListToArticleVoList(records));

    }

    @Override
    public Result getHotArticles() {
        List<Article> articleHotList = getOrderedArticles(Article::getViewCounts);
        return Result.success(ArticleToArticleHotOrNewVo(articleHotList));
    }

    @Override
    public Result getNewArticles() {
        List<Article> articleNewList = getOrderedArticles(Article::getCreateDate);
        return Result.success(ArticleToArticleHotOrNewVo(articleNewList));
    }

    @Override
    public Result getArchives() {
        return Result.success(articleMapper.getArticleArchivesByDate());
    }

    @Override
    public Result detailArticle(Long id) {
        Article article = lambdaQuery().eq(Article::getId, id).one();

        if(article == null) return Result.fail(ARTICLE_NOT_EXIST.getCode(),ARTICLE_NOT_EXIST.getMsg());

        threadService.updateViewCount(this, article);//线程池异步刷新阅读数

        ArticleVo articleVo = ArticleToArticleVo(article, true);

        return Result.success(articleVo);
    }

    /**
     * 通过条件排序文章
     * @param Condition
     * @return
     */
    private List<Article> getOrderedArticles(SFunction<Article, Object> Condition) {
        List<Article> articleList = lambdaQuery()
                .select(Article::getId, Article::getTitle)
                .orderByDesc(Condition)
                .last("limit " + HOT_NEW_ARTICLE_LIMIT)
                .list();
        return articleList;
    }

    /**
     * 将最新或者最热列表转换为ArticleHotAndNewVo返回前端
     * @param articleHotOrNewList
     * @return
     */
    private List<ArticleHotAndNewVo> ArticleToArticleHotOrNewVo(List<Article> articleHotOrNewList) {
        return articleHotOrNewList.stream()
                .map(article -> BeanUtil.copyProperties(article, ArticleHotAndNewVo.class))
                .collect(Collectors.toList());
    }

    /**
     * 将Article封装为ArticleListVo
     * @param records
     * @return
     */
    private List<ArticleVo> ArticleListToArticleVoList(List<Article> records) {
        return records.stream().map(article -> ArticleToArticleVo(article,false)).collect(Collectors.toList());

    }

    /**
     * 将Article对象转换为ArticleListVo对象
     * @param article
     * @return
     */
    private ArticleVo ArticleToArticleVo(Article article, boolean isDetail) {
        ArticleVo articleVo = BeanUtil.copyProperties(article, ArticleVo.class);
        setArticleVoTags(articleVo);
        setArticleVoAuthor(article, articleVo);
        formatArticleVoAuthorCreateDate(article, articleVo);

        if(isDetail){
            setArticleVoBodyContent(article, articleVo);
            setArticleVoBodyCategory(article, articleVo);
        }

        return articleVo;
    }

    /**
     * 设置articleVo的分类属性
     * @param article
     * @param articleVo
     */
    private void setArticleVoBodyCategory(Article article, ArticleVo articleVo) {
        articleVo.setCategory(
                BeanUtil.copyProperties
                        (categoryService.lambdaQuery()
                                .select(Category::getId,Category::getAvatar,Category::getCategoryName)
                                .eq(Category::getId, article.getCategoryId()).one(), CategoryVo.class));
    }

    /**
     * 设置articleVo的body属性
     * @param article
     * @param articleVo
     */
    private void setArticleVoBodyContent(Article article, ArticleVo articleVo) {
        ArticleBody articleBody = articleBodyService.lambdaQuery()
                .select(ArticleBody::getContent).
                eq(ArticleBody::getArticleId, article.getId()).one();
        articleVo.setBody(BeanUtil.copyProperties(articleBody,ArticleBodyVo.class));
    }

    /**
     * 将article中CreateDate的时间戳转换为时间并添加到ArticleVo
     * @param article
     * @param articleVo
     */
    private void formatArticleVoAuthorCreateDate(Article article, ArticleVo articleVo) {
        String CreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(article.getCreateDate()));
        articleVo.setCreateDate(CreateTime);
    }

    /**
     * 设置articlelistVo的作者
     * @param article
     * @param articleVo
     */
    private void setArticleVoAuthor(Article article, ArticleVo articleVo) {
        articleVo.setAuthor(userService.lambdaQuery()
                .select(SysUser::getAccount)
                .eq(SysUser::getId,article.getAuthorId())
                .one().getAccount());
    }

    /**
     * 设置ArticleVo的Tags属性
     * @param articleVo
     */
    private void setArticleVoTags(ArticleVo articleVo) {
        articleVo.setTags(tagService.getTagByArticleId(articleVo.getId()));
    }


}
