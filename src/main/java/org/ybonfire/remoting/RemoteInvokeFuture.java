package org.ybonfire.remoting;

import org.ybonfire.remoting.callback.IRemotingInvokeCallback;
import org.ybonfire.remoting.exception.ReadTimeoutException;
import org.ybonfire.remoting.exception.RemotingInvokeExecuteException;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 远程调用Future
 *
 * @author yuanbo
 * @date 2022-12-21 13:54
 */
public class RemoteInvokeFuture {
    private final AtomicReference<RemotingRequestFutureStateEnum> state =
        new AtomicReference<>(RemotingRequestFutureStateEnum.LAUNCH);
    private CompletableFuture<IRemotingCommand<?>> responseFuture = new CompletableFuture<>();
    private final String requestId;
    private final IRemotingInvokeCallback callback;
    private final long timeoutTimestamp;

    public RemoteInvokeFuture(final String requestId, final IRemotingInvokeCallback callback,
        final long timeoutTimestamp) {
        this.requestId = requestId;
        this.callback = callback;
        this.timeoutTimestamp = timeoutTimestamp;
    }

    /**
     * @description: 通过响应唤醒等待
     * @param:
     * @return:
     * @date: 2022/12/21 14:13:33
     */
    public void completeWithResponse(final IRemotingResponse<?> response) {
        if (state.compareAndSet(RemotingRequestFutureStateEnum.INFLIGHT, RemotingRequestFutureStateEnum.RESPOND)
            || state.compareAndSet(RemotingRequestFutureStateEnum.LAUNCH, RemotingRequestFutureStateEnum.RESPOND)) {
            if (isTimeout()) {
                responseFuture.complete(RemoteResponseBuilder.fail(requestId, ReadTimeoutException::new));
            } else {
                responseFuture.complete(response);
            }

            // 触发回调
            if (callback != null) {
                callback.onResponse(responseFuture.join());
            }
        }
    }

    /**
     * @description: 通过异常唤醒等待
     * @param:
     * @return:
     * @date: 2022/12/21 17:53:46
     */
    public void completeWithException(final Supplier<Throwable> supplier) {
        completeWithResponse(RemoteResponseBuilder.fail(requestId, supplier));
    }

    /**
     * @description: 等待响应
     * @param:
     * @return:
     * @date: 2022/12/21 14:16:36
     */
    public IRemotingCommand<?> waitResponse(final long timeoutMillis) throws InterruptedException {
        try {
            return this.responseFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            return RemoteResponseBuilder.fail(requestId, () -> new RemotingInvokeExecuteException(e));
        } catch (TimeoutException e) {
            return RemoteResponseBuilder.fail(requestId, ReadTimeoutException::new);
        }
    }

    public CompletableFuture<IRemotingCommand<?>> getResponseFuture() {
        return responseFuture;
    }

    public String getRequestId() {
        return requestId;
    }

    /**
     * @description: 请求发送成功回调
     * @param:
     * @return:
     * @date: 2022/10/20 17:49:15
     */
    public void onLaunchSuccess() {
        state.compareAndSet(RemotingRequestFutureStateEnum.LAUNCH, RemotingRequestFutureStateEnum.INFLIGHT);
    }

    /**
     * @description: 判断请求是否发送成功
     * @param:
     * @return:
     * @date: 2022/12/21 15:28:01
     */
    public boolean isLaunchSuccess() {
        return state.get() != null && state.get().getCode() > RemotingRequestFutureStateEnum.LAUNCH.getCode();
    }

    /**
     * @description: 判断是否超时
     * @param:
     * @return:
     * @date: 2022/12/21 15:44:42
     */
    public boolean isTimeout() {
        return System.currentTimeMillis() > timeoutTimestamp;
    }
}
