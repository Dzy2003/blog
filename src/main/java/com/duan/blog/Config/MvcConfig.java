package com.duan.blog.Config;

import com.duan.blog.Interceptor.LoginInterceptor;
import com.duan.blog.Service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    ISysUserService userService;
    //跨域配置，不可设置为*，不安全, 前后端分离项目，可能域名不一致
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:8080");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor(userService))
                .addPathPatterns(
                        "/test",
                        "/comments/create/change",
                        "/articles/publish"
                );
    }
}
