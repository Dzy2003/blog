package com.duan.blog.Interceptor;

import cn.hutool.json.JSONUtil;
import com.duan.blog.dto.Result;
import com.duan.blog.utils.ErrorCode;
import com.duan.blog.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author 白日
 * @date Created in 2023/9/30 16:11
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("===========登录拦截器生效==============");
        //不存在，拦截，返回401状态码
        if(UserHolder.getUser() == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSONUtil.toJsonStr(result));
            return false;
        }
        //放行
        return true;
    }
}
