package org.ybonfire.remoting.exception;

/**
 * 远程调用中断异常
 *
 * @author yuanbo
 * @date 2022-09-09 17:19
 */
public final class RemotingInvokeInterruptedException extends BaseLiteRpcException {

    public RemotingInvokeInterruptedException() {}

    public RemotingInvokeInterruptedException(final String message) {
        super(message);
    }

    public RemotingInvokeInterruptedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RemotingInvokeInterruptedException(final Throwable cause) {
        super(cause);
    }
}
