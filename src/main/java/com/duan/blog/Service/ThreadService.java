package com.duan.blog.Service;

import com.duan.blog.pojo.Article;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;

public interface ThreadService {

    void updateViewCount(IArticleService articleService, Article article);

}
