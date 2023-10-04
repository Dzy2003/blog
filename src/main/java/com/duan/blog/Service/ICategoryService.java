package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Category;

public interface ICategoryService extends IService<Category> {

    Result getAllCategories();

    Result getCategoriesDetail();

    Result getCategoriesDetailById(Long id);
}
