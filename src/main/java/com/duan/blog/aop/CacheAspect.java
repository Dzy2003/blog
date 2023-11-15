package com.duan.blog.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.duan.blog.Service.ThreadService;
import com.duan.blog.aop.annotation.CacheAnnotation;
import com.duan.blog.utils.SimpleRedisLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ibatis.javassist.bytecode.SignatureAttribute;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.TimeUnit;

/**
 * @author 白日
 * @date Created in 2023/10/6 9:35
 */
@Component
@Slf4j
@Aspect
public class CacheAspect {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Pointcut("@annotation(com.duan.blog.aop.annotation.CacheAnnotation)")
    public void pointCut(){}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        CacheAnnotation annotation = signature.getMethod().getAnnotation(CacheAnnotation.class);
        String cacheKey = annotation.KeyPrefix() + getKeyKeySuffix(joinPoint.getArgs(),signature.getName());
        //log.info(cacheKey);
        String redisData = stringRedisTemplate.opsForValue().get(cacheKey);
        //缓存命中
        if(StrUtil.isNotBlank(redisData)){
            log.info("走了缓存~~~,{},{}",cacheKey,annotation.cacheName());
            return JSONUtil.toBean(redisData,signature.getReturnType());
        }
        Object Result = joinPoint.proceed();
        //解决缓存穿透
        if(BeanUtil.isEmpty(Result)) stringRedisTemplate.opsForValue().set(cacheKey, "");
        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, annotation.cacheName());
        try {
            if(!lock.tryLock(10L)){
                log.info("我没拿到锁，我需要等。。。" + Thread.currentThread().getId());
                Thread.sleep(500);
                return around(joinPoint);
            }
            log.info("缓存未命中，缓存重建,{},{},{}",cacheKey,annotation.cacheName(),Thread.currentThread().getId());
            rebuildCache(Result,annotation.expire(),cacheKey);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
        return BeanUtil.copyProperties(Result, signature.getReturnType());
    }


    /**
     * 通过参数列表和缓存的名称和注解方法的参数列表来生成key的后缀
     * @param args
     * @param methodName
     * @return
     */
    private String getKeyKeySuffix(Object[] args,String methodName) {
        StringBuilder params = new StringBuilder();
        for (Object parameter : args) {
            params.append(StrUtil.toString(parameter));
        }
        if (StrUtil.isNotEmpty(params)) {
            //加密 以防出现key过长以及字符转义获取不到的情况,保证KeyPrefix后的字段唯一
            params = new StringBuilder(DigestUtils.md5Hex(params.append(methodName).toString()));
        }
        return params.toString();
    }

    /**
     * 重建缓存
     * @param mysqlData
     * @param expire
     * @param cacheKey
     * @throws InterruptedException
     */
    private void rebuildCache(Object mysqlData,Long expire,String cacheKey) throws InterruptedException {
        //Thread.sleep(10000);
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(mysqlData),expire, TimeUnit.SECONDS);
        log.info("重建缓存成功。。。。"+Thread.currentThread().getId());
    }
}
