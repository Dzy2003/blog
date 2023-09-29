package com.duan.blog;

import com.duan.blog.Mapper.TagMapper;
import com.duan.blog.entity.Tag;
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

@SpringBootTest
@Slf4j
class BlogApplicationTests {
    @Autowired
    TagMapper mapper;
    @Test
    void contextLoads() {
        Long timeStamp = 1625433900l;
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault());
        System.out.println(time);

        long timestamp = time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println(timeStamp);


    }

}
