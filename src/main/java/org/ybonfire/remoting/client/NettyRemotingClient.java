package org.ybonfire.remoting.client;

import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.callback.IRemotingInvokeCallback;
import org.ybonfire.remoting.client.connection.ConnectionManager;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;
import org.ybonfire.remoting.util.PreCondition;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Netty的远程调用客户端
 *
 * @author yuanbo
 * @date 2022-12-19 10:29
 */
public class NettyRemotingClient extends AbstractLifeCycle implements IRemotingClient {
    private final String id = UUID.randomUUID().toString();
    private final RemotingClientOptions options;
    private final ConnectionManager connectionManager;

    public NettyRemotingClient(final RemotingClientOptions options) {
        this.options = options;
        this.connectionManager = new ConnectionManager(this.options, this.id);
    }

    /**
     * @description: 同步远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:19:38
     */
    @Override
    public IRemotingResponse<?> invoke(final Endpoint endpoint, final IRemotingRequest<?> request,
        final long timeoutMillis) {
        // 参数、状态预检
        ensure(endpoint, request, timeoutMillis);

        // 建立连接
        final long startTime = System.currentTimeMillis();
        final Connection connection = connectionManager.getOrCreate(endpoint, timeoutMillis);

        // 发送请求
        final long remainingTime = timeoutMillis - (System.currentTimeMillis() - startTime);
        return (IRemotingResponse<?>)connection.invoke(request, remainingTime);
    }

    /**
     * @description: 异步远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:24:01
     */
    @Override
    public void invokeAsync(final Endpoint endpoint, final IRemotingRequest<?> request,
        final IRemotingInvokeCallback callback, long timeoutMillis) {
        // 参数、状态预检
        ensure(endpoint, request, timeoutMillis);

        // 建立连接
        final long startTime = System.currentTimeMillis();
        final Connection connection = connectionManager.getOrCreate(endpoint, timeoutMillis);

        // 发送请求
        final long remainingTime = timeoutMillis - (System.currentTimeMillis() - startTime);
        connection.invokeAsync(request, callback, remainingTime);
    }

    /**
     * @description: 异步远程调用
     * @param:
     * @return:
     * @date: 2022/12/20 09:41:38
     */
    @Override
    public CompletableFuture<IRemotingCommand<?>> invokeAsync(final Endpoint endpoint,
        final IRemotingRequest<?> request, final long timeoutMillis) {
        // 参数、状态预检
        ensure(endpoint, request, timeoutMillis);

        // 建立连接
        final long startTime = System.currentTimeMillis();
        final Connection connection = connectionManager.getOrCreate(endpoint, timeoutMillis);

        // 发送请求
        final long remainingTime = timeoutMillis - (System.currentTimeMillis() - startTime);
        return connection.invokeAsync(request, remainingTime);
    }

    /**
     * @description: 单向远程调用
     * @param:
     * @return:
     * @date: 2022/12/19 10:24:23
     */
    @Override
    public void oneway(final Endpoint endpoint, final IRemotingRequest<?> request) {
        // 参数、状态预检
        ensure(endpoint, request, options.getDefaultRemoteInvokeTimeoutMillis());

        try {
            // 建立连接
            final long timeoutMillis = options.getDefaultRemoteInvokeTimeoutMillis();
            final Connection connection = connectionManager.getOrCreate(endpoint, timeoutMillis);

            // 发送请求
            connection.invokeOneway(request);
        } catch (Throwable ex) {
            // ignore
        }
    }

    /**
     * @description: 服务启动流程
     * @param:
     * @return:
     * @date: 2022/10/17 17:14:38
     */
    @Override
    protected void onStart() {
        connectionManager.start();
    }

    /**
     * @description: 服务关闭流程
     * @param:
     * @return:
     * @date: 2022/10/17 17:14:40
     */
    @Override
    protected void onShutdown() {
        connectionManager.shutdown();
    }

    /**
     * @description: 预检处理
     * @param:
     * @return:
     * @date: 2022/12/21 18:14:07
     */
    private void ensure(final Endpoint endpoint, final IRemotingRequest<?> request, final long timeoutMillis) {
        // 参数校验
        PreCondition.notNull(endpoint, "endpoint");
        PreCondition.notNull(request, "request");
        PreCondition.assertTrue(timeoutMillis > 0L, "timeoutMillis must be greater than 0L");
        acquireOK();
    }
}
