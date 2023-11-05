package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Category;

public interface ICategoryService extends IService<Category> {
    /**
     * 获取所有分类
     * @return
     */
    Result getAllCategories();

    /**
     * 获取分类详情
     * @return
     */
    Result getCategoriesDetail();

    /**
     * 根据id获取分类详情
     * @param id
     * @return
     */
    Result getCategoriesDetailById(Long id);
}
