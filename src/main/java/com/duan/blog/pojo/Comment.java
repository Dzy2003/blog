package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 白日
 * @Date Created in 2023/10/2 9:46
 */
@Data
@TableName("tb_comment")
public class Comment {
    @TableId(value = "id", type = IdType.AUTO)

    private Long id;

    private String content;

    private Long createDate;

    private Long articleId;

    private Long authorId;

    private Long parentId;

    private Long toUid;

    private Integer level;
}
