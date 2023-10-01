package com.duan.blog.Service;

import com.duan.blog.entity.Article;

public interface ThreadService {

    void updateViewCount(IArticleService articleService, Article article);

}
