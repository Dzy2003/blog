package com.duan.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duan.blog.entity.ArticleBody;
import com.duan.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}
