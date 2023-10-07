package com.duan.blog.utils;

public class RedisConstants {
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30l;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_Article_TTL = 30L;
    public static final String CACHE_Article_KEY = "cache:article:";
    public static final String CACHE_TYPELIST_KEY = "cache:typelist:";

    public static final String LOCK_Article_KEY = "lock:article:";
    public static final Long LOCK_Article_TTL = 10L;


    public static final String BLOG_LIKED_KEY = "Article:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String USER_SIGN_KEY = "sign:";
}
