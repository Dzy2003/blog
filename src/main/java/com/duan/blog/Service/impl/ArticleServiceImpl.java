package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.duan.blog.vo.ArticleVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {
    @Resource
    ITagService tagService;
    @Resource
    ISysUserService userService;
    @Override
    public Result listArticles(PageInfo pageInfo) {
        if(pageInfo == null) return Result.fail(1,"No page info");
        List<Article> records = lambdaQuery().orderByDesc(Article::getWeight)
                .orderByDesc(Article::getCreateDate)
                .page(new Page<>(pageInfo.getPage(), pageInfo.getPageSize()))
                .getRecords();
        //log.info("数据库查询数据：" + records.toString());
        return Result.success(recordToArticleVo(records));

    }

    /**
     * 将Article封装为ArticleVo
     * @param records
     * @return
     */
    private List<ArticleVo> recordToArticleVo(List<Article> records) {
        return records.stream().map((article -> {
            Long authorId = article.getAuthorId();
            String CreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(article.getCreateDate()));
            ArticleVo articleVo = BeanUtil.copyProperties(article, ArticleVo.class);
            articleVo.setTags(tagService.getTagByArticleId(articleVo.getId()));
            articleVo.setAuthor(userService.lambdaQuery()
                    .select(SysUser::getAccount)
                    .eq(SysUser::getId,authorId)
                    .one().getAccount());
            articleVo.setCreateDate(CreateTime);
            return articleVo;
        })).collect(Collectors.toList());

    }


}
