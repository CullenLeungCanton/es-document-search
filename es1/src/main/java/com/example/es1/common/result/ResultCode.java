package com.example.es1.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    BUSINESS_ERROR(1001, "业务异常"),
    FILE_UPLOAD_ERROR(1002, "文件上传失败"),
    FILE_PARSE_ERROR(1003, "文件解析失败"),
    ES_SEARCH_ERROR(1004, "搜索失败"),
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    USER_DISABLED(2004,"账号已被禁用"),
    PASSWORD_ERROR(2005, "密码错误"),
    OLD_PASSWORD_ERROR(2006, "旧密码错误"),
    PERMISSION_DENIED(2007, "权限不足");


    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}