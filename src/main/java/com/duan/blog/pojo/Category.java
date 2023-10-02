package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:12
 */
@Data
@TableName("tb_category")
public class Category {

    private Long id;

    private String avatar;

    private String categoryName;

    private String description;
}
