package org.ybonfire.remoting.exception.handler;

import org.ybonfire.remoting.RemoteResponseBuilder;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;

import io.netty.channel.ChannelHandlerContext;

/**
 * 这里添加类的注释【强制】
 *
 * @author yuanbo
 * @date 2022-12-22 16:07
 */
public class ServerExceptionHandler {
    private static final ServerExceptionHandler INSTANCE = new ServerExceptionHandler();

    private ServerExceptionHandler() {}

    /**
     * @description: 异常处理
     * @param:
     * @return:
     * @date: 2022/12/22 16:09:33
     */
    public void handle(final IRemotingRequest<?> request, final Exception ex, final ChannelHandlerContext context) {
        final IRemotingResponse<?> response = RemoteResponseBuilder.fail(request.getCommandId(), () -> ex);
        context.channel().writeAndFlush(response);
    }

    /**
     * @description: 获取单例
     * @param:
     * @return:
     * @date: 2022/12/22 16:08:20
     */
    public static ServerExceptionHandler getInstance() {
        return INSTANCE;
    }
}
