package com.duan.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 白日
 * @date Created in 2023/10/28 16:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeInfo {

    String account;

    String oldPassword;

    String newPassword;
}
