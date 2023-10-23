package com.duan.blog;

import cn.hutool.core.bean.BeanUtil;
import com.duan.blog.Mapper.ArticleMapper;
import com.duan.blog.Mapper.TagMapper;

import com.duan.blog.Service.IArticleService;
import com.duan.blog.Service.IArticleTagService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.UserDTO;
import com.duan.blog.pojo.Article;
import com.duan.blog.pojo.SysUser;
import com.duan.blog.utils.CacheClient;
import com.duan.blog.utils.RedisConstants;
import com.duan.blog.utils.RedisData;
import com.duan.blog.utils.UserHolder;
import com.duan.blog.vo.ArchivesVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.duan.blog.utils.RedisConstants.FEED_KEY;
import static com.duan.blog.utils.SystemConstants.PAGE_SIZE;

@SpringBootTest
@Slf4j
class BlogApplicationTests {
    @Resource
    ArticleMapper articleMapper;
    @Resource
    IArticleService articleService;
    @Autowired
    TagMapper mapper;
    @Autowired
    CacheClient cacheClient;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    ISysUserService userService;

    @Test
    public void testRunnable() throws Exception {
        String s = "bd8f3a7fdeda9bf81d6f60c88338a14f";
        test(s, (a) -> a.toLowerCase());
    }
    public <T,R> void test(T r,Function<T,R> function){
        R apply = function.apply(r);
        System.out.println(apply);
    }
    @Test
    public void testCallback(){
        Supplier<List<ArchivesVo>> getArticleArchivesByDate = articleMapper::getArticleArchivesByDate;
        System.out.println(getArticleArchivesByDate.get());
    }
    @Test
    public void preCache(){
        cacheClient.setWithLogicalExpire(
                RedisConstants.CACHE_Article_KEY + 1,
                articleService.lambdaQuery().eq(Article::getId, 1).one(),
                10l,
                TimeUnit.SECONDS);
    }
    @Test
    public void testSelect(){
        Set<ZSetOperations.TypedTuple<String>> feedArticles =
                stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                        FEED_KEY + 3,
                        0, 1698056309000l, 0, PAGE_SIZE);
        List<Long> ids = feedArticles.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        System.out.println(ids);
    }
}


