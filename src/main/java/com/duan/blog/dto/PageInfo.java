package com.duan.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {
    Integer page;

    Integer pageSize;

    private Long categoryId;

    private Long tagId;
}
