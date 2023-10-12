package com.duan.blog.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.json.JSONUtil;
import com.duan.blog.Service.ThreadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.duan.blog.utils.RedisConstants.CACHE_NULL_TTL;

/**
 * @author 白日
 * @date Created in 2023/10/10 14:50
 */
@Slf4j
@Component
public class CacheClient {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 向redis中存入数据并指定TTL
     * @param key 键
     * @param value 数据
     * @param time TTL
     * @param unit TTL的单位
     * @param <T> 数据类型
     */
    public  <T> void set(String key, T value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 向redis中存入数据并指定逻辑TTL
     * @param key 键
     * @param value 数据
     * @param time TTL
     * @param unit TTL的单位
     * @param <T> 数据类型
     */
    public  <T> void setWithLogicalExpire(String key, T value, Long time, TimeUnit unit){
        //封装RedisData
        RedisData data = new RedisData();
        data.setData(value);
        //计算逻辑过期时间
        data.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(data));
    }

    /**
     * 删除redis中的缓存
     * @param key 缓存的键
     */
    public void delete(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取缓存的字符串数据
     * @param key
     * @return
     */
    public String getCache(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存的数据
     * @param key 键
     * @param type 返回值类型
     * @param <T> 返回数据
     * @return
     */
    public <T> T getCache(String key, Class<T> type){
        String s = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(s)) return null;
        return JSONUtil.toBean(s,type);
    }

    /**
     * 查询缓存并解决缓存穿透(请求数据库和缓存中都不存在的数据)，
     * @param key 键
     * @param type 返回值类型
     * @param dbSupplier 数据库数据
     * @param time 缓存有效期
     * @param unit 时间单位
     * @param <R> 返回数据
     * @return
     */
    public <R> R queryWithPassThrough(
            String key, Class<R> type, Supplier<R> dbSupplier, Long time, TimeUnit unit) {

        String CacheStr = getCache(key);
        // 缓存为空说明数据库和缓存中都不存在该数据(缓存穿透)
        if("".equals(CacheStr)){
            return null;
        }
        //缓存命中直接将数据返回
        if(CacheStr != null) {
            return JSONUtil.toBean(CacheStr,type);
        }

        R dbData = dbSupplier.get();
        if(dbData == null){
            set(key, "",CACHE_NULL_TTL,TimeUnit.MINUTES );
            return null;
        }
        set(key, dbData, time, unit);
        return dbData;
    }

    /**
     * 互斥锁解决缓存击穿
     * @param key 缓存的key
     * @param type 返回值类型(class)
     * @param dbSupplier 获取数据库的数据
     * @param expire 过期时间
     * @param unit 时间单位
     * @param <R> 返回数据类型
     * @return
     */
    public <R> R queryWithMutex(String key,Class<R> type, Supplier<R> dbSupplier, Long expire, TimeUnit unit){

        String CacheStr = getCache(key);
        // 缓存为空说明数据库和缓存中都不存在该数据(缓存穿透)
        if("".equals(CacheStr)){
            log.info("缓存穿透~~~");
            return null;
        }
        //缓存命中直接将数据返回
        if(CacheStr != null) {
            log.info("走了缓存~~~");
            return JSONUtil.toBean(CacheStr,type);
        }
        //互斥锁重建缓存
        R dbData = null;
        SimpleRedisLock simpleRedisLock = new SimpleRedisLock(stringRedisTemplate,key.substring(key.indexOf(":")));
        try {
            //已经有线程开始重构，其余线程等待
            if(!simpleRedisLock.tryLock(20l)){
                log.info("我没拿到锁，我需要等。。。" + Thread.currentThread().getId());
                Thread.sleep(500);
                return queryWithMutex(key,type,dbSupplier,expire,unit);
            }
            log.info("我拿到锁，进行缓存重建,{},{}",key,Thread.currentThread().getId());
            dbData = dbSupplier.get();
            Thread.sleep(15000);
            //缓存穿透
            if(dbData == null){
                set(key, "",CACHE_NULL_TTL,TimeUnit.MINUTES );
                return null;
            }
            set(key,JSONUtil.toJsonStr(dbData),expire,unit);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            simpleRedisLock.unlock();
        }
        return dbData;
    }


    /**
     * 逻辑过期方案解决缓存击穿
     * @param key 缓存的key
     * @param type 返回值类型(class)
     * @param dbSupplier 获取数据库的数据
     * @param expire 过期时间
     * @param unit 时间单位
     * @param <R> 返回数据类型
     * @return
     */
    @Resource
    ThreadService threadService;
    public <R> R queryWithLogicalExpire(String key,Class<R> type, Supplier<R> dbSupplier, Long expire, TimeUnit unit){

        RedisData redisData = getCache(key,RedisData.class);
        //判断数据是否过期
        R cache = BeanUtil.toBean(redisData.getData(), type);
        if(redisData.getExpireTime().isAfter(LocalDateTime.now())) return cache;
        log.info("缓存过期");
        SimpleRedisLock simpleRedisLock = new SimpleRedisLock(stringRedisTemplate,key.substring(key.indexOf(":")));
        log.info("我拿到锁，开始异步线程重构缓存");
        if(simpleRedisLock.tryLock(10l)){
            try {
                threadService.rebuildCache(
                        new RedisData(LocalDateTime.now().plusSeconds(unit.toSeconds(expire)),dbSupplier.get()),
                        key,stringRedisTemplate);
            }catch (Exception e){
                throw new RuntimeException();
            }finally {
                simpleRedisLock.unlock();
            }
        }
        return cache;
    }
}
