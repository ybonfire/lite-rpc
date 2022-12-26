package org.ybonfire.remoting.client;

import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.callback.IRemotingInvokeCallback;
import org.ybonfire.remoting.lifecycle.ILifeCycle;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;

import java.util.concurrent.CompletableFuture;

/**
 * 远程调用客户端接口
 *
 * @author yuanbo
 * @date 2022-12-19 10:18
 */
public interface IRemotingClient extends ILifeCycle {

    /**
     * @description: 同步远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:19:38
     */
    IRemotingResponse<?> invoke(final Endpoint endpoint, final IRemotingRequest<?> request, final long timeoutMillis);

    /**
     * @description: 异步远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:24:01
     */
    void invokeAsync(final Endpoint endpoint, final IRemotingRequest<?> request, final IRemotingInvokeCallback callback,
        final long timeoutMillis);

    /**
     * @description: 异步远程调用
     * @param:
     * @return:
     * @date: 2022/12/20 09:41:38
     */
    CompletableFuture<IRemotingCommand<?>> invokeAsync(final Endpoint endpoint, final IRemotingRequest<?> request,
        final long timeoutMillis);

    /**
     * @description: 单向远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:24:23
     */
    void oneway(final Endpoint endpoint, final IRemotingRequest<?> request);
}
