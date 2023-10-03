package com.duan.blog.dto;

import com.duan.blog.pojo.Tag;
import com.duan.blog.vo.CategoryVo;
import lombok.Data;

import java.util.List;

/**
 * @author 白日
 * @date Created in 2023/10/3 13:12
 */
@Data
public class ArticleInfo {
    private Long id;

    private ArticleBodyParam body;

    private CategoryVo category;

    private String summary;

    private List<Tag> tags;

    private String title;
}
