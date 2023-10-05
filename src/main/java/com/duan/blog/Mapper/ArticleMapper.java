package com.duan.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duan.blog.pojo.Article;
import com.duan.blog.vo.ArchivesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Select("SELECT YEAR(FROM_UNIXTIME(create_date/1000)) as year" +
            ",month(FROM_UNIXTIME(create_date/1000)) Month,COUNT(*) as count " +
            "FROM tb_article GROUP BY year,Month;")
    List<ArchivesVo> getArticleArchivesByDate();

    IPage<Article> listArticle(Page<Article> page,
                               Long categoryId,
                               Long tagId,
                               String year,
                               String month);
}
