package com.duan.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 白日
 * @date Created in 2023/10/28 15:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditInfo {
    private Long id;
    private String email;
    private String mobilePhoneNumber;
    private String nickname;
}
