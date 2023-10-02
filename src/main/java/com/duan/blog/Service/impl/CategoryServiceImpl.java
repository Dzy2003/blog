package com.duan.blog.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.CategoryMapper;
import com.duan.blog.Service.ICategoryService;
import com.duan.blog.pojo.Category;
import org.springframework.stereotype.Service;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:25
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
