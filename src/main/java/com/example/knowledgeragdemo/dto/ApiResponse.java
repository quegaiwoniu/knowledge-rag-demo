package com.example.knowledgeragdemo.dto;

/**
 * 项目内所有 HTTP 接口统一使用的响应包装对象。
 *
 * <p>这样做的目的是让前端处理更简单、更稳定：
 * 所有接口都统一返回 success、data、message 三个字段。</p>
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    /**
     * 构造一个成功响应，默认 message 为 "OK"。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK");
    }

    /**
     * 构造一个带自定义 message 的成功响应。
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /**
     * 构造一个失败响应。
     *
     * <p>当前这个小型 demo 先只返回错误 message，保持结构简单。
     * 如果后面需要，我们可以继续扩展错误码、traceId 等字段。</p>
     */
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
