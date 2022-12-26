package org.ybonfire.remoting.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.common.NettyEventTypeEnum;
import org.ybonfire.remoting.common.concurrent.SerializingExecutor;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.lifecycle.ILifeCycle;
import org.ybonfire.remoting.util.PreCondition;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 连接事件处理器
 *
 * @author yuanbo
 * @date 2022-12-16 15:05
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientNettyConnectEventHandler extends ChannelOutboundHandlerAdapter implements ILifeCycle {
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final SerializingExecutor serializingExecutor = new SerializingExecutor(1024);

    /**
     * @description: 连接事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:18:48
     */
    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress,
        final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CONNECT, ctx, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 断开连接事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:18:53
     */
    @Override
    public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.DISCONNECT, ctx, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 关闭连接事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:19:04
     */
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CLOSE, ctx, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 异常捕获事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:20:12
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.EXCEPTION_CAUGHT, ctx, cause);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 构造NettyConnectEventProcessTask
     * @param:
     * @return:
     * @date: 2022/12/16 17:28:22
     */
    private NettyConnectEventProcessTask build(final NettyEventTypeEnum type, final ChannelHandlerContext context,
        final Throwable cause) {
        return new NettyConnectEventProcessTask(type, Connection.getFromChannel(context.channel()), cause);
    }

    @Override
    public void start() {
        if (isStarted.compareAndSet(false, true)) {
            this.serializingExecutor.start();
        }
    }

    @Override
    public boolean isStarted() {
        return isStarted.get();
    }

    @Override
    public void shutdown() {
        if (isStarted.compareAndSet(true, false)) {
            this.serializingExecutor.shutdown();
        }
    }

    /**
     * @description: NettyConnect事件处理任务
     * @author: yuanbo
     * @date: 2022/12/16
     */
    private static class NettyConnectEventProcessTask implements Runnable {
        private final NettyEventTypeEnum type;
        private final Connection connection;
        private final Throwable cause;

        private NettyConnectEventProcessTask(final NettyEventTypeEnum type, final Connection connection,
            final Throwable cause) {
            this.type = type;
            this.connection = connection;
            this.cause = cause;
        }

        @Override
        public void run() {
            process();
        }

        /**
         * @description: 处理Netty连接事件
         * @param:
         * @return:
         * @date: 2022/12/16 17:44:39
         */
        private void process() {
            PreCondition.notNull(type);
            PreCondition.notNull(connection);

            switch (type) {
                case CONNECT:
                case DISCONNECT:
                case CLOSE:
                case CHANNEL_ACTIVE:
                case USER_TRIGGERED:
                case CHANNEL_INACTIVE:
                case CHANNEL_REGISTERED:
                case CHANNEL_UNREGISTERED:
                    log.info("Receive netty connect event:[{}]. Connection:[{}]", type, connection);
                    break;
                case EXCEPTION_CAUGHT:
                    log.error("Receive netty connection event:[{}]. Connection:[{}]. Exception:[{}]", type, connection,
                        cause);
                    break;
                default:
                    break;
            }
        }
    }
}
