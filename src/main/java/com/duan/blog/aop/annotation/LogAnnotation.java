package com.duan.blog.aop.annotation;

import java.lang.annotation.*;

/**
 * 记录日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    String module() default "";

    String operator() default "";
}
