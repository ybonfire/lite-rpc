package org.ybonfire.remoting;

/**
 * 响应状态枚举
 *
 * @author yuanbo
 * @date 2022-12-15 17:06
 */
public enum ResponseStatusEnum {
    /**
     * 成功
     */
    SUCCESS(0, "success"),
    /**
     * 请求发送失败
     */
    REQUEST_SEND_FAILED(100, "request send failed"),
    /**
     * 响应等待超时
     */
    RESPONSE_READ_TIMEOUT(101, "response read timeout"),
    /**
     * 连接状态异常
     */
    CONNECTION_IS_NOT_OK(103, "connection state is not ok"),
    /**
     * 不支持的请求类型
     */
    NOT_SUPPORTED_REQUEST_CODE(200, "not supported request code"),
    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION(1000, "Unknown Exception"),;

    private final int code;
    private final String description;

    ResponseStatusEnum(final Integer code, final String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ResponseStatusEnum of(final Integer code) {
        if (code == null) {
            return null;
        }

        for (final ResponseStatusEnum status : ResponseStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }

        return null;
    }

    public static ResponseStatusEnum fromException(final Class<? extends Throwable> clazz) {
        // TODO
        return UNKNOWN_EXCEPTION;
    }
}
