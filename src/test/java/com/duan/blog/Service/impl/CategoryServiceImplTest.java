package com.duan.blog.Service.impl;

import com.duan.blog.Service.ICategoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CategoryServiceImplTest {
    @Resource
    ICategoryService categoryService;
    @Test
    public void getAllCategories(){
        System.out.println(categoryService.getAllCategories());
    }
}