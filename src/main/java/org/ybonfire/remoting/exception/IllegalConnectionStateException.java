package org.ybonfire.remoting.exception;

/**
 * 连接状态非法异常
 *
 * @author yuanbo
 * @date 2022-12-21 17:21
 */
public class IllegalConnectionStateException extends BaseLiteRpcException {

    public IllegalConnectionStateException() {}

    public IllegalConnectionStateException(final String message) {
        super(message);
    }

    public IllegalConnectionStateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalConnectionStateException(final Throwable cause) {
        super(cause);
    }

}