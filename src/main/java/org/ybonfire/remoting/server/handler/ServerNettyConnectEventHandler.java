package org.ybonfire.remoting.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.common.NettyEventTypeEnum;
import org.ybonfire.remoting.common.concurrent.SerializingExecutor;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.lifecycle.ILifeCycle;
import org.ybonfire.remoting.server.connection.ConnectionManager;
import org.ybonfire.remoting.util.PreCondition;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 连接事件处理器
 *
 * @author yuanbo
 * @date 2022-12-16 15:05
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerNettyConnectEventHandler extends ChannelInboundHandlerAdapter implements ILifeCycle {
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final SerializingExecutor serializingExecutor = new SerializingExecutor(1024);
    private final ConnectionManager connectionManager;

    public ServerNettyConnectEventHandler(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * @description: Channel注册事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:19:10
     */
    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        final Connection connection = new Connection(ctx.channel());
        connectionManager.add(connection);
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CHANNEL_REGISTERED, connection, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: Channel取消注册事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:19:17
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        final Connection connection = new Connection(ctx.channel());
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CHANNEL_UNREGISTERED, connection, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: Channel活跃事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:19:26
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final Connection connection = new Connection(ctx.channel());
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CHANNEL_ACTIVE, connection, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: Channel不活跃事件
     * @param:
     * @return:
     * @date: 2022/12/16 15:19:34
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        final Connection connection = new Connection(ctx.channel());
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.CHANNEL_INACTIVE, connection, null);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 用户事件触发
     * @param:
     * @return:
     * @date: 2022/12/16 15:20:03
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object event) throws Exception {
        super.userEventTriggered(ctx, event);
        final Connection connection = new Connection(ctx.channel());
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.USER_TRIGGERED, connection, null);
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
        final Connection connection = new Connection(ctx.channel());
        final NettyConnectEventProcessTask task = build(NettyEventTypeEnum.EXCEPTION_CAUGHT, connection, cause);
        serializingExecutor.execute(task);
    }

    /**
     * @description: 构造NettyConnectEventProcessTask
     * @param:
     * @return:
     * @date: 2022/12/16 17:28:22
     */
    private NettyConnectEventProcessTask build(final NettyEventTypeEnum type, final Connection connection,
        final Throwable cause) {
        return new NettyConnectEventProcessTask(type, connection, cause);
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
