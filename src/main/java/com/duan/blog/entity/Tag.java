package com.duan.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_tag")
public class Tag {

    private Long id;

    private String avatar;

    private String tagName;
}
