package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Service.*;
import com.duan.blog.aop.annotation.LogAnnotation;
import com.duan.blog.dto.*;
import com.duan.blog.pojo.*;
import com.duan.blog.utils.CacheClient;
import com.duan.blog.utils.ErrorCode;
import com.duan.blog.utils.RedisConstants;
import com.duan.blog.utils.UserHolder;
import com.duan.blog.vo.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.duan.blog.utils.ErrorCode.ARTICLE_NOT_EXIST;
import static com.duan.blog.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.duan.blog.utils.RedisConstants.FEED_KEY;
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
    @Resource
    IFollowService followService;

    @Override
//    @CacheAnnotation(KeyPrefix = RedisConstants.CACHE_Article_KEY)
    @LogAnnotation(module = "Article", operator = "查询文章列表")
    public Result listArticles(PageInfo pageInfo) {
        return Result.success(ArticleListToArticleVoList(getArticles(pageInfo)));
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
            cancel(id);
        }else{
            like(id);
        }
        return Result.success(null);
    }

    @Override
    public Result getArticleLikes(Long id) {
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(
                BLOG_LIKED_KEY + id, 0, 4);
        if(isNullOrEmpty(top5)){
            return Result.success(null);
        }
        List<UserDTO> result = userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAvatar, SysUser::getNickname, SysUser::getAccount)
                .in(SysUser::getId, top5.stream().map(Long::valueOf).collect(Collectors.toList()))
                .last("ORDER BY FIELD(id," + String.join(",", top5) + ")")
                .list()
                .stream()
                .map(sysUser -> BeanUtil.copyProperties(sysUser, UserDTO.class))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 数据库中获取分页Article列表
     * @param pageInfo 文章查询条件
     * @return Article集合
     */
    private List<Article> getArticles(PageInfo pageInfo) {
        return articleMapper.listArticle(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()),
                pageInfo.getCategoryId(), pageInfo.getTagId(),
                pageInfo.getYear(), pageInfo.getMonth(),pageInfo.getAuthorId()).getRecords();
    }

    @Override
    public Result getBlogOfFollow(Long max, Integer offset) {
        Set<ZSetOperations.TypedTuple<String>> feedArticles =
                stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                FEED_KEY + UserHolder.getUser().getId(),
                0, max, offset, PAGE_SIZE);
        if(isNullOrEmpty(feedArticles)){
            return Result.success(null);
        }
        return Result.success(getScrollResult(feedArticles));
    }

    @Override
    public Result deleteArticleByAuthorId(Long articleId, Long authorId) {
        if(!authorId.equals(UserHolder.getUser().getId())){
            articleMapper.deleteById(articleId);
            return Result.success(null);
        }
        return Result.fail(ErrorCode.NOT_AUTHOR.getCode(),ErrorCode.NOT_AUTHOR.getMsg());
    }

    /**
     * 封装滚动查询返回实体类
     * @param feedArticles 本页的数据（K：文章id，V：时间戳）
     * @return ScrollResult（List，max，offset）
     */
    private ScrollResult getScrollResult(Set<ZSetOperations.TypedTuple<String>> feedArticles) {
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(getArticleVos(feedArticles));
        scrollResult.setMinTime(getMinTime(feedArticles));
        scrollResult.setOffset(1);
        return scrollResult;
    }

    /**
     * 拿到当前查询数据的最小时间戳
     * @param feedArticles 本页的数据（K：文章id，V：时间戳）
     * @return 本次查询最小时间戳
     */
    private Long getMinTime(Set<ZSetOperations.TypedTuple<String>> feedArticles) {
        return feedArticles.stream()
                .map(ZSetOperations.TypedTuple::getScore)
                .toList()
                .get(feedArticles.size() - 1)
                .longValue();
    }

    /**
     * 拿到当页数据
     * @param feedArticles 本页的数据（K：文章id，V：时间戳）
     * @return 按照feedArticles文章id的顺序拿到文章列表
     */
    private List<ArticleVo> getArticleVos(Set<ZSetOperations.TypedTuple<String>> feedArticles) {
        List<Long> ids = feedArticles.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return ArticleListToArticleVoList(
                lambdaQuery()
                        .in(Article::getId,ids)
                        .last("ORDER BY FIELD(id,"
                                + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")")
                        .list());
    }

    /**
     * 点赞
     * @param id 文章ID
     */
    private void like(Long id) {
        lambdaUpdate().setSql("liked = liked + 1").eq(Article::getId, id).update();
        stringRedisTemplate.opsForZSet().add(BLOG_LIKED_KEY + id,
                UserHolder.getUser().getId().toString(),
                System.currentTimeMillis());
    }

    /**
     * 取消点赞
     * @param id 文章ID
     */
    private void cancel(Long id) {
        if(lambdaUpdate().setSql("liked = liked - 1").eq(Article::getId, id).update()){
            stringRedisTemplate.opsForZSet().remove(BLOG_LIKED_KEY + id,
                    UserHolder.getUser().getId().toString());
        }
    }

    /**
     * 更新文章
     * @param articleInfo 文章数据
     * @return 更新的文章ID
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
        this.insertArticleTag(articleInfo,article.getId());
        return article.getId();
    }

    /**
     * 插入文章
     * @param articleInfo 文章信息
     * @return 插入的文章ID
     */
    private Long savaArticle(ArticleInfo articleInfo) {
        Article article = BeanUtil.copyProperties(articleInfo, Article.class);
        article.setCommentCounts(0);
        article.setViewCounts(0);
        article.setAuthorId(UserHolder.getUser().getId());
        article.setBodyId(-1L);
        article.setCategoryId(articleInfo.getCategory().getId());
        article.setCreateDate(System.currentTimeMillis());
        article.setLiked(0);
        boolean success = save(article);
        insertArticleBody(articleInfo,article.getId());
        insertArticleTag(articleInfo,article.getId());
        if(success){
            feedArticleToFans(article.getId());
        }
        return article.getId();
    }

    /**
     * 用户发表文章后将文章ID推送给粉丝
     * @param articleId 文章ID
     */
    private void feedArticleToFans(Long articleId) {
        followService.lambdaQuery()
                .select(Follow::getUserId)
                .eq(Follow::getFollowUserId, UserHolder.getUser().getId())
                .list().stream()
                .map(Follow::getUserId)
                .toList()
                .forEach(FeedId ->
                    stringRedisTemplate.opsForZSet().add(RedisConstants.FEED_KEY + FeedId,
                            articleId.toString(), System.currentTimeMillis())
                );
    }

    /**
     * 插入文章体
     * @param articleInfo 文章信息（无Id信息）
     * @param articleId 文章Id
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
     * @param articleInfo 文章信息，
     * @param articleId 插入的文章id
     */
    private void insertArticleTag(ArticleInfo articleInfo,Long articleId) {
        articleTagService.saveBatch(articleInfo.getTags().stream().map(tag -> {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setTagId(tag.getId());
            articleTag.setArticleId(articleId);
            return articleTag;
        }).toList());
    }

    /**
     * 通过条件排序文章
     * @param Condition 排序条件
     * @return 排序后的文章列表
     */
    private List<Article> getOrderedArticles(SFunction<Article, Object> Condition) {
        return lambdaQuery()
                .select(Article::getId, Article::getTitle)
                .orderByDesc(Condition)
                .last("limit " + HOT_NEW_ARTICLE_LIMIT)
                .list();
    }

    /**
     * 将最新或者最热列表转换为ArticleHotAndNewVo返回前端
     * @param articleHotOrNewList 文章列表
     * @return ArticleHotAndNewVo（id和title）集合
     */
    private List<ArticleHotAndNewVo> ArticleToArticleHotOrNewVo(List<Article> articleHotOrNewList) {
        return articleHotOrNewList.stream()
                .map(article -> BeanUtil.copyProperties(article, ArticleHotAndNewVo.class))
                .toList();
    }

    /**
     * 将Article封装为ArticleListVo
     * @param records Article集合
     * @return ArticleListVo集合
     */
    private List<ArticleVo> ArticleListToArticleVoList(List<Article> records) {
        return records.stream().map(article -> ArticleToArticleVo(article,false)).collect(Collectors.toList());

    }

    /**
     * 将Article对象转换为ArticleListVo对象
     * @param article Article实体
     * @return ArticleListVo实体
     */
    private ArticleVo ArticleToArticleVo(Article article, boolean isDetail) {
        ArticleVo articleVo = BeanUtil.copyProperties(article, ArticleVo.class);
        setArticleVoTags(articleVo);
        setArticleVoAuthor(article.getAuthorId(), articleVo);
        setArticleVoLiked(articleVo);
        formatArticleVoAuthorCreateDate(article.getCreateDate(), articleVo);
        setArticleVoLikes(article, articleVo);
        if(isDetail){
            setArticleVoBodyContent(article.getId(), articleVo);
            setArticleVoBodyCategory(article.getCategoryId(), articleVo);
        }

        return articleVo;
    }

    /**
     * 设置articleVo的likes属性
     * @param article
     * @param articleVo
     */
    private void setArticleVoLikes(Article article, ArticleVo articleVo) {
        articleVo.setLikes(stringRedisTemplate.opsForZSet().count(BLOG_LIKED_KEY + article.getId()
                ,0, System.currentTimeMillis()));
    }

    /**
     * 设置articleVo的isLike属性
     */
    private void setArticleVoLiked(ArticleVo articleVo) {
        if(UserHolder.getUser() == null) return;
        articleVo.setIsLiked(isBlogLiked(articleVo.getId()));

    }

    /**
     * 获取用户当前用户是否点赞过该博客
     * @param articleId 文章id
     * @return 未点赞true，点赞false
     */
    private Boolean isBlogLiked(Long articleId) {
        Double score = stringRedisTemplate.opsForZSet().score(
                BLOG_LIKED_KEY + articleId,
                UserHolder.getUser().getId().toString());
        return score != null;
    }

    /**
     * 设置articleVo的分类属性
     * @param categoryId 文章所属的分类ID
     * @param articleVo 设置分类后的articleVo
     */
    private void setArticleVoBodyCategory(Long categoryId, ArticleVo articleVo) {
        Category category = categoryService
                .lambdaQuery()
                .select(Category::getId, Category::getAvatar, Category::getCategoryName)
                .eq(Category::getId, categoryId)
                .one();
        articleVo.setCategory(BeanUtil.copyProperties(category, CategoryVo.class));
    }

    /**
     * 设置articleVo的body文章内容属性
     * @param articleId 文章ID
     * @param articleVo 设置文章内容后的articleVo
     */
    private void setArticleVoBodyContent(Long articleId, ArticleVo articleVo) {
        ArticleBody articleBody = articleBodyService
                .lambdaQuery()
                .select(ArticleBody::getContent).
                eq(ArticleBody::getArticleId, articleId).one();
        articleVo.setBody(BeanUtil.copyProperties(articleBody,ArticleBodyVo.class));
    }

    /**
     * 将article中CreateDate的时间戳转换为时间并设置到ArticleVo
     * @param articleCreateDate 文章的创建时间（时间戳）
     * @param articleVo 设置创建时间后的articleVo
     */
    private void formatArticleVoAuthorCreateDate(Long articleCreateDate, ArticleVo articleVo) {
        String CreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(articleCreateDate));
        articleVo.setCreateDate(CreateTime);
    }

    /**
     * 设置articleListVo的作者
     * @param authorId 文章作者
     * @param articleVo 展示给前端的articleVo
     */
    private void setArticleVoAuthor(Long authorId, ArticleVo articleVo) {
        articleVo.setAuthor(BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getNickname,SysUser::getId,SysUser::getAvatar)
                .eq(SysUser::getId,authorId)
                .one(), UserDTO.class));
    }

    /**
     * 设置ArticleVo的Tags属性
     * @param articleVo 展示给前端的articleVo
     */
    private void setArticleVoTags(ArticleVo articleVo) {
        articleVo.setTags(tagService.getTagByArticleId(articleVo.getId()));
    }

    /**
     * 返回集合类型是否没有数据
     * @param collection 所有集合父类
     * @param <T> 具体集合类型
     * @return 无true，有false
     */
    private <T extends Collection<?>> boolean isNullOrEmpty(T collection){
        return collection == null || collection.isEmpty();
    }
}
