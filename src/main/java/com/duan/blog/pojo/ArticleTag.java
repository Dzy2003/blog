package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 白日
 * @date Created in 2023/10/3 14:16
 */
@Data
@TableName("tb_article_tag")
public class ArticleTag {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    Long articleId;

    Long tagId;

}
