package com.duan.blog.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData<R> {
    private LocalDateTime expireTime;
    private R data;
    public RedisData(){}

    public RedisData(LocalDateTime expireTime, R data) {
        this.expireTime = expireTime;
        this.data = data;
    }
}
