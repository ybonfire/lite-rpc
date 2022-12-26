package org.ybonfire.remoting.exception;

/**
 * 响应超时异常
 *
 * @author yuanbo
 * @date 2022-12-21 14:18
 */
public class ReadTimeoutException extends BaseLiteRpcException {
    public ReadTimeoutException() {}

    public ReadTimeoutException(final String message) {
        super(message);
    }

    public ReadTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ReadTimeoutException(final Throwable cause) {
        super(cause);
    }
}
