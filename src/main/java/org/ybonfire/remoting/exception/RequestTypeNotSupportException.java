package org.ybonfire.remoting.exception;

/**
 * 不支持的请求类型异常
 *
 * @author yuanbo
 * @date 2022-09-09 14:25
 */
public class RequestTypeNotSupportException extends BaseLiteRpcException {

    public RequestTypeNotSupportException() {}

    public RequestTypeNotSupportException(final String message) {
        super(message);
    }

    public RequestTypeNotSupportException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RequestTypeNotSupportException(final Throwable cause) {
        super(cause);
    }

}
