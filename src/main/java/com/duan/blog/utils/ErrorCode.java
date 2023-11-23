package com.duan.blog.utils;

import lombok.Getter;

@Getter
public enum  ErrorCode {

    PARAMS_ERROR(10001,"参数有误"),
    ACCOUNT_PWD_NOT_EXIST(10002,"用户名或密码不存在"),
    ACCOUNT_PWD_NOT_INPUT(10003,"用户名或密码未输入"),
    NO_LOGIN(10004,"未登录"),
    ACCOUNT_EXIST(10005,"用户名已存在"),
    USER_NOT_EXIST(10006,"该用户不存在"),
    ARTICLE_NOT_EXIST(20001,"该文章不存在或已被删除"),
    NOT_AUTHOR(20002,"该文章不属于你"),
    DATA_ERROR(30001, "获取数据失败"),
    REPETITIVE_OPERATION(30002, "请勿重复操作"),
    NO_PERMISSION(70001,"无访问权限"),
    SESSION_TIME_OUT(90001,"会话超时");


    private final int code;
    private final String msg;

    ErrorCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
}
