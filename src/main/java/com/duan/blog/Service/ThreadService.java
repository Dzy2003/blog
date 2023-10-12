package com.duan.blog.Service;

import com.duan.blog.pojo.Article;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public interface ThreadService {

    void updateViewCount(IArticleService articleService, Article article);


    <R> void rebuildCache(R mysqlData, String cacheKey,StringRedisTemplate stringRedisTemplate);
}
