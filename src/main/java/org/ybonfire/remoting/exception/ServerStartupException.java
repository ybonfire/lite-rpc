package org.ybonfire.remoting.exception;

/**
 * 服务端启动异常
 *
 * @author yuanbo
 * @date 2022-10-12 17:42
 */
public class ServerStartupException extends BaseLiteRpcException {

    public ServerStartupException() {}

    public ServerStartupException(final String message) {
        super(message);
    }

    public ServerStartupException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServerStartupException(final Throwable cause) {
        super(cause);
    }
}
