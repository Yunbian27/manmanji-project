package com.yunbian27.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户
    USERNAME_EXISTS(400, "用户名已存在"),
    EMAIL_EXISTS(400, "邮箱已被注册"),
    USER_NOT_FOUND(400, "用户不存在"),
    USER_BANNED(400, "用户已被禁用"),

    // ========== Provider管理模块错误 1xxx ==========
    PROVIDER_NOT_FOUND(1001, "LLM Provider 不存在"),
    PROVIDER_ALREADY_EXISTS(1002, "LLM Provider 已存在"),
    PROVIDER_CONFIG_READ_FAILED(1004, "读取 Provider 配置失败"),
    PROVIDER_CONFIG_WRITE_FAILED(1005, "写入 Provider 配置失败"),
    PROVIDER_TEST_FAILED(1006, "Provider 连通性测试失败"),
    PROVIDER_DEFAULT_CANNOT_DELETE(1007, "默认 Provider 不可删除"),
    MODULE_NOT_FOUND(1008, "模块不存在");


    private final int code;
    private final String message;


}
