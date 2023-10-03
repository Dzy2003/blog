package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.CategoryMapper;
import com.duan.blog.Service.ICategoryService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Category;
import com.duan.blog.vo.CategoryVo;
import org.springframework.stereotype.Service;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:25
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public Result getAllCategories() {
        return Result.success(lambdaQuery()
                .select(Category::getId,Category::getAvatar,Category::getCategoryName)
                .list().stream().map(category -> BeanUtil.copyProperties(category, CategoryVo.class)));
    }
}
