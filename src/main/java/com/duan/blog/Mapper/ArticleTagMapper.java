package com.duan.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duan.blog.pojo.ArticleBody;
import com.duan.blog.pojo.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

}
