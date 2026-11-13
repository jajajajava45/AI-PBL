package com.pbl.selection.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应包装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;       // 0=成功，其他=失败
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
