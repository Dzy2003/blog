package com.duan.blog.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAnnotation {

    long expire() default 1 * 60 * 1000;

    String KeyPrefix() default "";

    String cacheName() default "";
}
