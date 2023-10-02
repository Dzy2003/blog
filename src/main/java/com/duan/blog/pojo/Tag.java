package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_tag")
public class Tag {

    private Long id;

    private String avatar;

    private String tagName;
}
