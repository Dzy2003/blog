package com.duan.blog.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.ArticleBodyMapper;
import com.duan.blog.Mapper.ArticleTagMapper;
import com.duan.blog.Service.IArticleBodyService;
import com.duan.blog.Service.IArticleTagService;
import com.duan.blog.pojo.ArticleBody;
import com.duan.blog.pojo.ArticleTag;
import org.springframework.stereotype.Service;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:25
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements IArticleTagService {

}
