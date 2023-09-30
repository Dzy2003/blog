package com.duan.blog.Interceptor;

import cn.hutool.core.util.StrUtil;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.Result;
import com.duan.blog.dto.UserDTO;
import com.duan.blog.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author 白日
 * @date Created in 2023/9/30 16:13
 * 拦截所有请求，没有token直接放行，有token则刷新UserHolder存储的用户信息和redis存放的token
 */

@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    ISysUserService userService;
    public RefreshTokenInterceptor(ISysUserService userService) {
        this.userService = userService;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("=================刷新拦截器生效===========================");
        String token = request.getHeader("Authorization");

        if(StrUtil.isBlank(token)) {
            return true;
        }
        //TODO:检测redis是否存在该token，不在则放行，在则刷新时间

        UserDTO currentUser = (UserDTO)userService.getCurrentUser(token).getData();

        log.info("Current user: " + currentUser);

        UserHolder.saveUser(currentUser);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
