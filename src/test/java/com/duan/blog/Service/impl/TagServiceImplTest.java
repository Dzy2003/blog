package com.duan.blog.Service.impl;

import com.duan.blog.Service.ITagService;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TagServiceImplTest {
    @Resource
    ITagService tagService;
    @Test
    public void testGetHotTags(){
        Result hotTags = tagService.getHotTags();
        System.out.println(hotTags);
    }

    @Test
    void getAllTags() {
        System.out.println(tagService.getAllTags());
    }
}