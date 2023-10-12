package com.duan.blog.Service.impl;

import cn.hutool.json.JSONUtil;
import com.duan.blog.Service.IArticleService;
import com.duan.blog.Service.ThreadService;
import com.duan.blog.pojo.Article;
import com.duan.blog.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
/**
 * 异步线程池
 */
public class ThreadServiceImpl implements ThreadService {

    @Override
    @Async("taskExecutor")
    /**
     * 异步刷新更新数
     */
    public void updateViewCount(IArticleService articleService, Article article) {
        articleService.lambdaUpdate()
                .set(Article::getViewCounts, article.getViewCounts()+1)
                .eq(Article::getId, article.getId())
                .eq(Article::getViewCounts,article.getViewCounts())
                .update();
        try {
            //睡眠5秒 证明不会影响主线程的使用
            Thread.sleep(5000);
            log.info("异步更新阅读数");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <R> void rebuildCache(R dbData,String cacheKey, StringRedisTemplate stringRedisTemplate) {
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(dbData));
        log.info("重建缓存成功。。。。"+Thread.currentThread().getId());
    }
}
