package com.duan.blog;

import com.duan.blog.Mapper.TagMapper;
import com.duan.blog.entity.Tag;
import com.duan.blog.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class BlogApplicationTests {
    @Autowired
    TagMapper mapper;
    @Test
    void contextLoads() {
        String token = JWTUtils.createToken(1L);
        String token2 ="eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2OTY5MjgwMjEsInVzZXJJZCI6bnVsbCwiaWF0IjoxNjk2MDM4OTg5fQ.4tRcLDJaHis5n6qVqYKCyqsyyGlvM-GUMh0we3jYrMI";
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token2);
        System.out.println(stringObjectMap.toString());
        System.out.println(stringObjectMap.get("userId"));

    }

}
