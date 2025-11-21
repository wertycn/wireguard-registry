package icu.debug.net.wg.service.entity;

import lombok.Data;

/**
 * AMIS 统一响应格式
 */
@Data
public class ApiResponse<T> {
    private int status = 0;  // 0 表示成功，AMIS 要求
    private String msg = "success";
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = -1;
        response.msg = msg;
        return response;
    }
}
