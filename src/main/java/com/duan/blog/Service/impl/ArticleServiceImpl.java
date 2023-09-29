package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Service.IArticleService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.Service.ITagService;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.entity.Article;
import com.duan.blog.entity.SysUser;
import com.duan.blog.vo.ArticleHotAndNewVo;
import com.duan.blog.vo.ArticleListVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public Result listArticles(PageInfo pageInfo) {
        if(pageInfo == null) return Result.fail(1,"No page info");
        List<Article> records = lambdaQuery().orderByDesc(Article::getWeight)
                .orderByDesc(Article::getCreateDate)
                .page(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()))
                .getRecords();
        //log.info("数据库查询数据：" + records.toString());
        return Result.success(ArticleToArticleVo(records));

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


    private List<ArticleHotAndNewVo> ArticleToArticleHotOrNewVo(List<Article> articleHotList) {
        return articleHotList.stream()
                .map(article -> BeanUtil.copyProperties(article, ArticleHotAndNewVo.class))
                .collect(Collectors.toList());

    }

    /**
     * 将Article封装为ArticleListVo
     * @param records
     * @return
     */
    private List<ArticleListVo> ArticleToArticleVo(List<Article> records) {
        return records.stream().map((article -> {
            Long authorId = article.getAuthorId();
            String CreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(article.getCreateDate()));
            ArticleListVo articlelistVo = BeanUtil.copyProperties(article, ArticleListVo.class);
            articlelistVo.setTags(tagService.getTagByArticleId(articlelistVo.getId()));
            articlelistVo.setAuthor(userService.lambdaQuery()
                    .select(SysUser::getAccount)
                    .eq(SysUser::getId,authorId)
                    .one().getAccount());
            articlelistVo.setCreateDate(CreateTime);
            return articlelistVo;
        })).collect(Collectors.toList());

    }


}
