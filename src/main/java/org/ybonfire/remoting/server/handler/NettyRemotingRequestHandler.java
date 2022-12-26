package org.ybonfire.remoting.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.exception.RequestTypeNotSupportException;
import org.ybonfire.remoting.exception.handler.ServerExceptionHandler;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.server.processor.IRequestProcessor;
import org.ybonfire.remoting.server.processor.RequestProcessorManager;

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
public class NettyRemotingRequestHandler extends SimpleChannelInboundHandler<IRemotingRequest<?>> {
    private final ExecutorService defaultExecutor = ForkJoinPool.commonPool();
    private final RequestProcessorManager requestProcessorManager;

    public NettyRemotingRequestHandler(final RequestProcessorManager requestProcessorManager) {
        this.requestProcessorManager = requestProcessorManager;
    }

    /**
     * @description: 接收远程调用请求
     * @param:
     * @return:
     * @date: 2022/12/16 14:56:40
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final IRemotingRequest<?> request) throws Exception {
        defaultExecutor.execute(() -> process(ctx, request));
    }

    /**
     * @description: 处理请求
     * @param:
     * @return:
     * @date: 2022/12/22 16:09:58
     */
    private void process(final ChannelHandlerContext ctx, final IRemotingRequest<?> request) {
        try {
            final RequestCodeEnum requestCode = RequestCodeEnum.of(request.getCode());
            final Optional<IRequestProcessor> processorOptional = requestProcessorManager.get(requestCode);
            if (processorOptional.isPresent()) {
                final IRequestProcessor processor = processorOptional.get();
                final Connection connection = new Connection(ctx.channel());
                processor.process(request, connection);
            } else {
                throw new RequestTypeNotSupportException();
            }
        } catch (Exception ex) {
            ServerExceptionHandler.getInstance().handle(request, ex, ctx);
        }
    }
}
