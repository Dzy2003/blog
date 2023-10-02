package com.duan.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duan.blog.pojo.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("select * FROM tb_tag where id in (SELECT tag_id from tb_article_tag where article_id = #{id});")
    List<Tag> selectTagById(@Param("id") Long id);

    @Select("SELECT tag_id FROM `tb_article_tag` GROUP BY tag_id ORDER BY COUNT(*)")
    List<Long> getHotTagId();
}
