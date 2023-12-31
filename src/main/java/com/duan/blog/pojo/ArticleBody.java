package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:10
 */
@Data
@TableName("tb_article_body")
public class ArticleBody {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String content;

    private String contentHtml;

    private Long articleId;
}
