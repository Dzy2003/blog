package com.duan.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duan.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

}
