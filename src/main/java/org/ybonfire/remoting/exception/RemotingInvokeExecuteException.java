package org.ybonfire.remoting.exception;

/**
 * 远程调用执行异常
 *
 * @author yuanbo
 * @date 2022-09-09 17:14
 */
public final class RemotingInvokeExecuteException extends BaseLiteRpcException {

    public RemotingInvokeExecuteException(final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public RemotingInvokeExecuteException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
