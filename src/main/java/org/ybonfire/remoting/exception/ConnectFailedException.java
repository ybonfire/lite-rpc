package org.ybonfire.remoting.exception;

/**
 * 连接失败异常
 *
 * @author yuanbo
 * @date 2022-09-09 16:29
 */
public final class ConnectFailedException extends BaseLiteRpcException {

    public ConnectFailedException() {}

    public ConnectFailedException(final String message) {
        super(message);
    }

    public ConnectFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConnectFailedException(final Throwable cause) {
        super(cause);
    }
}
