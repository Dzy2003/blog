package com.duan.blog.Controller;

import cn.hutool.core.bean.BeanUtil;
import com.duan.blog.Service.ICategoryService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Category;
import com.duan.blog.vo.CategoryVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 白日
 * @date Created in 2023/10/3 9:19
 */
@RestController
@RequestMapping("/categorys")
public class CategoryController {
    @Resource
    ICategoryService categoryService;

    @GetMapping
    public Result listCategories(){
        return categoryService.getAllCategories();

    }

}
