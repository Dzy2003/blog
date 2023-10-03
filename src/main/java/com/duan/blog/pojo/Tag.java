package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_tag")
public class Tag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String avatar;

    private String tagName;
}
