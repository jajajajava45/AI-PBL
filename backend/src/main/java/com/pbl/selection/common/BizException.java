package com.pbl.selection.common;

import lombok.Getter;

/**
 * 业务异常：message 直接透出给前端
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 404 资源不存在 */
    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    /** 409 冲突 / 状态不允许 */
    public static BizException conflict(String message) {
        return new BizException(409, message);
    }

    /** 403 无权限 */
    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }
}
