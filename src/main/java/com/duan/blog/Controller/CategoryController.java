package com.duan.blog.Controller;

import cn.hutool.core.bean.BeanUtil;
import com.duan.blog.Service.ICategoryService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Category;
import com.duan.blog.vo.CategoryVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author 白日
 * @date Created in 2023/10/3 9:19
 */
@RestController
@RequestMapping("/categorys")
public class CategoryController {
    @Resource
    ICategoryService categoryService;

    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GetMapping
    public Result listCategories(){
        return categoryService.getAllCategories();
    }

    /**
     * 获取所有分类详情
     * @return
     */
    @GetMapping("/detail")
    public Result listCategoriesDetail(){
        return categoryService.getCategoriesDetail();
    }

    /**
     * 获取某个分类详情
     * @param id 分类id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result CategoryDetail(@PathVariable Long id){
        return categoryService.getCategoriesDetailById(id);
    }

}
