package org.ybonfire.remoting.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.RemoteInvokeFuture;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Netty远程调用请求处理器
 *
 * @author yuanbo
 * @date 2022-12-16 14:55
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyRemotingResponseHandler extends SimpleChannelInboundHandler<IRemotingResponse<?>> {
    private final ExecutorService defaultExecutor = ForkJoinPool.commonPool();

    /**
     * @description: 接收远程调用请求
     * @param:
     * @return:
     * @date: 2022/12/16 14:56:40
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final IRemotingResponse<?> response) throws Exception {
        final Connection connection = Connection.getFromChannel(ctx.channel());
        defaultExecutor.submit(() -> process(response, connection));
    }

    /**
     * @description: 处理响应
     * @param:
     * @return:
     * @date: 2022/12/21 22:23:40
     */
    private void process(final IRemotingResponse<?> response, final Connection connection) {
        final String responseId = response.getCommandId();
        try {
            final Optional<RemoteInvokeFuture> futureOptional = connection.tryToGetRemoteInvokeFuture(responseId);
            if (!futureOptional.isPresent()) {
                log.warn("Receive Response, ResponseId:[{}]. But not found corresponding RemoteInvokeFuture",
                    responseId);
                return;
            }

            futureOptional.ifPresent(future -> future.completeWithResponse(response));
        } finally {
            connection.removeRemoteInvokeFuture(responseId);
        }
    }
}
