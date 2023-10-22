package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Service.*;
import com.duan.blog.aop.annotation.CacheAnnotation;
import com.duan.blog.aop.annotation.LogAnnotation;
import com.duan.blog.dto.ArticleInfo;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.dto.UserDTO;
import com.duan.blog.pojo.*;
import com.duan.blog.utils.CacheClient;
import com.duan.blog.utils.RedisConstants;
import com.duan.blog.utils.UserHolder;
import com.duan.blog.vo.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    IArticleTagService articleTagService;
    @Resource
    CacheClient cacheClient;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    @CacheAnnotation(KeyPrefix = RedisConstants.CACHE_Article_KEY)
    @LogAnnotation(module = "Article", operator = "查询文章列表")
    public Result listArticles(PageInfo pageInfo) {
//        if(pageInfo == null) return Result.fail(1,"No page info");
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(Article::getWeight)
//                .orderByDesc(Article::getCreateDate);
//        if(pageInfo.getCategoryId() != null) queryWrapper.eq(Article::getCategoryId,pageInfo.getCategoryId());
//        if(pageInfo.getTagId() != null) {
//            queryWrapper.in(Article::getId,articleTagService.lambdaQuery()
//                    .select(ArticleTag::getArticleId)
//                    .eq(ArticleTag::getTagId,pageInfo.getTagId())
//                    .list()
//                    .stream().map(ArticleTag::getArticleId)
//                    .collect(Collectors.toList()));
//        }
//        List<Article> records = articleMapper.selectPage(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()),
//                queryWrapper.orderByDesc(Article::getWeight)
//                        .orderByDesc(Article::getCreateDate)).getRecords();
        //log.info("数据库查询数据：" + records.toString());

        return Result.success(ArticleListToArticleVoList(getArticles(pageInfo)));
    }

    /**
     * 数据库中获取Article列表
     * @param pageInfo
     * @return
     */
    private List<Article> getArticles(PageInfo pageInfo) {
        return articleMapper.listArticle(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()),
                pageInfo.getCategoryId(), pageInfo.getTagId(),
                pageInfo.getYear(), pageInfo.getMonth()).getRecords();
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
    public Result detailArticle(Long id,Boolean isEdit) {
        //逻辑过期方案做缓存
//        Article articleVo = cacheClient.queryWithLogicalExpire(
//                RedisConstants.CACHE_Article_KEY + id,
//                Article.class,
//                () -> lambdaQuery().eq(Article::getId, id).one(),
//                RedisConstants.CACHE_Article_TTL,
//                TimeUnit.MINUTES
//        );
        //互斥锁方案做缓存
        ArticleVo articleVo = cacheClient.queryWithMutex(
                RedisConstants.CACHE_Article_KEY + id,
                ArticleVo.class,
                () -> ArticleToArticleVo(lambdaQuery().eq(Article::getId, id).one(),true),
                RedisConstants.CACHE_Article_TTL,
                TimeUnit.MINUTES
        );
        if(articleVo == null) return Result.fail(ARTICLE_NOT_EXIST.getCode(),ARTICLE_NOT_EXIST.getMsg());
        //if(isEdit) threadService.updateViewCount(this, article);//线程池异步刷新阅读数
        return Result.success(articleVo);
    }

    @Override
    @Transactional
    public Result insertOrUpdateArticle(ArticleInfo articleInfo) {

        if(articleInfo.getId() == null){
            return Result.success(savaArticle(articleInfo));
        }
        return Result.success(updateArticle(articleInfo));
    }

    @Override
    public Result likeArticle(Long id) {

        if(isBlogLiked(id)){
            like(id);
        }else{
            cancel(id);
        }
        return Result.success(null);
    }

    @Override
    public Result getArticleLikes(Long id) {
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(
                RedisConstants.BLOG_LIKED_KEY + id, 0, 4);
        List<UserDTO> result = userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAvatar, SysUser::getNickname, SysUser::getAccount)
                .in(SysUser::getId, top5.stream().map(Long::valueOf).collect(Collectors.toList()))
                .last("ORDER BY FIELD(id," + top5.stream().collect(Collectors.joining(",")) + ")")
                .list()
                .stream()
                .map(sysUser -> BeanUtil.copyProperties(sysUser, UserDTO.class))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 点赞
     * @param id
     */
    private void like(Long id) {
        lambdaUpdate().setSql("liked = liked + 1").eq(Article::getId, id).update();
        stringRedisTemplate.opsForZSet().add(RedisConstants.BLOG_LIKED_KEY + id,
                UserHolder.getUser().getId().toString(),
                System.currentTimeMillis());
    }

    /**
     * 取消点赞
     * @param id
     */
    private void cancel(Long id) {
        if(lambdaUpdate().setSql("liked = liked - 1").eq(Article::getId, id).update()){
            stringRedisTemplate.opsForZSet().remove(RedisConstants.BLOG_LIKED_KEY + id,
                    UserHolder.getUser().getId().toString());
        }
    }

    /**
     * 更新文章
     * @param articleInfo
     * @return
     */
    private Long updateArticle(ArticleInfo articleInfo) {
        Article article = BeanUtil.copyProperties(articleInfo, Article.class);
        article.setCategoryId(articleInfo.getCategory().getId());
        updateById(article);
        //删除缓存
        cacheClient.delete(RedisConstants.CACHE_Article_KEY + articleInfo.getId());
        //更新article_body表
        ArticleBody articleBody = BeanUtil.copyProperties(articleInfo.getBody(), ArticleBody.class);
        articleBodyService.lambdaUpdate()
                .eq(ArticleBody::getArticleId,article.getId()).update(articleBody);

        ////更新article_tag表，一条文章涉及多个标签，修改时先将原本标签记录删除后再进行增加
        articleTagService.lambdaUpdate().eq(ArticleTag::getArticleId, articleInfo.getId()).remove();
        this.insertArticleTag(articleInfo,article);
        return article.getId();
    }

    /**
     * 插入文章
     * @param articleInfo
     * @return
     */
    private Long savaArticle(ArticleInfo articleInfo) {
        Article article = BeanUtil.copyProperties(articleInfo, Article.class);
        article.setCommentCounts(0);
        article.setViewCounts(0);
        article.setAuthorId(UserHolder.getUser().getId());
        article.setBodyId(-1l);
        article.setCategoryId(articleInfo.getCategory().getId());
        article.setCreateDate(System.currentTimeMillis());
        article.setLiked(0);
        save(article);
        insertArticleBody(articleInfo,article.getId());
        insertArticleTag(articleInfo,article);

        return article.getId();
    }

    /**
     * 插入文章体
     * @param articleInfo
     * @param articleId
     */
    private void insertArticleBody(ArticleInfo articleInfo,Long articleId) {
        ArticleBody articleBody = BeanUtil.copyProperties(articleInfo.getBody(), ArticleBody.class);
        articleBody.setArticleId(articleId);
        articleBodyService.save(articleBody);
        lambdaUpdate()
                .set(Article::getBodyId, articleBody.getId())
                .eq(Article::getId,articleId)
                .update();
    }

    /**
     * 插入文章标签
     * @param articleInfo
     * @param article
     */
    private void insertArticleTag(ArticleInfo articleInfo,Article article) {
        articleTagService.saveBatch(articleInfo.getTags().stream().map(tag -> {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setTagId(tag.getId());
            articleTag.setArticleId(article.getId());
            return articleTag;
        }).collect(Collectors.toList()));
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
        setArticleVoLiked(articleVo);
        formatArticleVoAuthorCreateDate(article, articleVo);
        if(isDetail){
            setArticleVoBodyContent(article, articleVo);
            setArticleVoBodyCategory(article, articleVo);
        }

        return articleVo;
    }

    /**
     * 设置articleVo的isLike属性
     * @param articleVo
     */
    private void setArticleVoLiked(ArticleVo articleVo) {
        if(UserHolder.getUser() == null) return;
        articleVo.setIsLiked(isBlogLiked(articleVo.getId()));

    }

    /**
     * 获取用户当前用户是否点赞过该博客
     * @param articleId
     * @return
     */
    private Boolean isBlogLiked(Long articleId) {
        Double score = stringRedisTemplate.opsForZSet().score(
                RedisConstants.BLOG_LIKED_KEY + articleId,
                UserHolder.getUser().getId().toString());
        return score == null;
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
        articleVo.setAuthor(BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getNickname,SysUser::getId,SysUser::getAvatar)
                .eq(SysUser::getId,article.getAuthorId())
                .one(), UserDTO.class));
    }

    /**
     * 设置ArticleVo的Tags属性
     * @param articleVo
     */
    private void setArticleVoTags(ArticleVo articleVo) {
        articleVo.setTags(tagService.getTagByArticleId(articleVo.getId()));
    }


}
