package com.duan.blog.Mapper;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TagMapperTest {
    @Resource
    TagMapper mapper;
    @Test
    public void testHotList(){
        List<Long> tagId = mapper.getHotTagId();
    }

}