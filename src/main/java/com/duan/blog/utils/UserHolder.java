package com.duan.blog.utils;

import com.duan.blog.dto.UserDTO;

/**
 * @author 白日
 * @date Created in 2023/9/30 16:09
 */

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }

    public static Long getUserID(){
        return tl.get().getId();
    }
}
