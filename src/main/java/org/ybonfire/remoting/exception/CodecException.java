package org.ybonfire.remoting.exception;

/**
 * 序列化异常
 *
 * @author yuanbo
 * @date 2022-12-15 14:49
 */
public class CodecException extends BaseLiteRpcException {
    public CodecException() {}

    public CodecException(final String message) {
        super(message);
    }

    public CodecException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CodecException(final Throwable cause) {
        super(cause);
    }
}
