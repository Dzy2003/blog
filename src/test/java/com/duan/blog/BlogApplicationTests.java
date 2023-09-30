package com.duan.blog;

import com.duan.blog.Mapper.TagMapper;
import com.duan.blog.entity.Tag;
import com.duan.blog.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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
        String s1 = DigestUtils.md5Hex("admin1mszlu!@#");
        String s2 = DigestUtils.md5Hex("admin1mszlu!@#");
        System.out.println(s1.equals(s2));


    }

}
