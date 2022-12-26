package org.ybonfire.remoting;

/**
 * 请求类型枚举
 *
 * @author yuanbo
 * @date 2022-12-15 17:03
 */
public enum RequestCodeEnum {
    /**
     * 心跳请求
     */
    HEARTBEAT(0, "heartbeat"),
    /**
     * 测试请求
     */
    TEST(-99, "test"),;

    private final int code;
    private final String description;

    RequestCodeEnum(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RequestCodeEnum of(final Integer code) {
        if (code == null) {
            return null;
        }

        for (final RequestCodeEnum request : RequestCodeEnum.values()) {
            if (request.code == code) {
                return request;
            }
        }

        return null;
    }
}
