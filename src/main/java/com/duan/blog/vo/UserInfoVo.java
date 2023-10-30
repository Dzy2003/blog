package com.duan.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 白日
 * @date Created in 2023/10/29 9:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoVo {
    private Long id;
    private String account;
    private String avatar;
    private Long createDate;
    private String email;
    private Long lastLogin;
    private String mobilePhoneNumber;
    private String nickname;
}
