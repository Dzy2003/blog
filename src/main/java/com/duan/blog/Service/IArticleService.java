package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.entity.Article;

public interface IArticleService extends IService<Article> {
    Result listArticles(PageInfo pageInfo);
}
