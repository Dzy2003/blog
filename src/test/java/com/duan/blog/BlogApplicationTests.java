package com.duan.blog;

import com.duan.blog.Mapper.TagMapper;
import com.duan.blog.entity.Tag;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class BlogApplicationTests {
    @Autowired
    TagMapper mapper;
    @Test
    void contextLoads() {
        List<Tag> integers = mapper.selectTagById(1l);
        log.info(integers.toString());
    }

}
