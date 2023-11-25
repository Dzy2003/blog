package com.duan.blog.vo;

import com.duan.blog.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 白日
 * @create 2023/11/24 21:58
 * @description 用户评论管理列表VO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CommentManageVo {
    // 评论id
    private Long commentId;
    //评论用户
    private UserDTO CommentUser;
    //评论时间
    private String createTime;
    //文章id
    private Long articleId;
    //文章标题
    private String articleTitle;
    //评论内容
    private String content;
}
