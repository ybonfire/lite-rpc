package org.ybonfire.remoting.exception;

/**
 * LiteRpc基础异常
 *
 * @author yuanbo
 * @date 2022-12-15 15:53
 */
public class BaseLiteRpcException extends RuntimeException {

    public BaseLiteRpcException() {}

    public BaseLiteRpcException(final String message) {
        super(message);
    }

    public BaseLiteRpcException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BaseLiteRpcException(final Throwable cause) {
        super(cause);
    }
}
