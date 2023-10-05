package com.duan.blog.aop;

import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.json.JSONUtil;
import com.duan.blog.aop.annotation.LogAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author 白日
 * @date Created in 2023/10/5 18:56
 * 日志切面
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Pointcut("@annotation(com.duan.blog.aop.annotation.LogAnnotation)")
    public void logPointCut(){}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        long executeTime = System.currentTimeMillis() - beginTime;
        recordLog(point, executeTime,result);
        return result;
    }

    private void recordLog(ProceedingJoinPoint point, long executeTime,Object result) {
        MethodSignature signature = (MethodSignature)point.getSignature();
        log.info("=====================log start================================");
        logAnnotationMessage(signature.getMethod().getAnnotation(LogAnnotation.class));
        logMethodMessage(point,signature);
        logArgs(point.getArgs());
        log.info("execute Result: {}",JSONUtil.toJsonStr(result));
        log.info("execute time : {} ms",executeTime);
        log.info("=====================log end================================");
    }

    private void logArgs(Object[] args) {
        log.info("params:{}", JSONUtil.toJsonStr(args[0]));
    }

    private void logMethodMessage(ProceedingJoinPoint point, MethodSignature signature) {
        log.info("request method:{}",point.getTarget().getClass().getName() + "." + signature.getName() + "()");
    }


    private void logAnnotationMessage(LogAnnotation annotation) {
        log.info("module:{}",annotation.module());
        log.info("operation:{}",annotation.operator());
    }


}
