package com.duan.blog.Service;

import com.duan.blog.pojo.Article;

public interface ThreadService {

    void updateViewCount(IArticleService articleService, Article article);

}
