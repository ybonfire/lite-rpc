package org.ybonfire.remoting.client.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.client.RemotingClientOptions;
import org.ybonfire.remoting.client.handler.ClientNettyConnectEventHandler;
import org.ybonfire.remoting.client.handler.NettyRemotingResponseHandler;
import org.ybonfire.remoting.codec.Decoder;
import org.ybonfire.remoting.codec.Encoder;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.exception.ConnectFailedException;
import org.ybonfire.remoting.exception.ConnectTimeoutException;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.util.NamedThreadFactory;
import org.ybonfire.remoting.util.NettyUtil;

import java.util.concurrent.TimeUnit;

/**
 * 远程调用连接工厂
 *
 * @author yuanbo
 * @date 2022-10-17 14:57
 */
@Slf4j
class ConnectionFactory extends AbstractLifeCycle {
    private final Bootstrap bootstrap = new Bootstrap();
    private final RemotingClientOptions options;
    private EventLoopGroup clientEventLoopGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private ClientNettyConnectEventHandler nettyConnectEventHandler;

    public ConnectionFactory(final RemotingClientOptions options) {
        this.options = options;
    }

    /**
     * @description: 启动流程
     * @param:
     * @return:
     * @date: 2022/10/17 17:22:40
     */
    @Override
    protected void onStart() {
        // eventLoopGroup
        final int eventLoopGroupThreadNums = options.getEventLoopThreadNums();
        clientEventLoopGroup =
            NettyUtil.newEventLoopGroup(eventLoopGroupThreadNums, new NamedThreadFactory("clientEventLoopGroup"));

        // executorGroup
        final int executorThreadNums = options.getWorkerThreadNums();
        defaultEventExecutorGroup = NettyUtil.newDefaultEventExecutorGroup(executorThreadNums,
            new NamedThreadFactory("defaultEventExecutorGroup"));

        // nettyConnectEventHandler
        this.nettyConnectEventHandler = new ClientNettyConnectEventHandler();
        this.nettyConnectEventHandler.start();

        // start bootstrap
        bootstrap.group(clientEventLoopGroup).channel(NettyUtil.getClientSocketChannelClass())
            .option(ChannelOption.SO_SNDBUF, options.getClientSocketSendBufferSize())
            .option(ChannelOption.SO_RCVBUF, options.getClientSocketReceiveBufferSize())
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(defaultEventExecutorGroup, new Encoder(), new Decoder(),
                        nettyConnectEventHandler, new NettyRemotingResponseHandler());
                }
            });
    }

    @Override
    protected void onShutdown() {
        // nettyConnectEventHandler
        this.nettyConnectEventHandler.shutdown();

        // clientEventLoopGroup
        if (this.clientEventLoopGroup != null) {
            this.clientEventLoopGroup.shutdownGracefully();
        }

        // executorGroup
        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    /**
     * @description: 建立指定地址的连接
     * @param:
     * @return:
     * @date: 2022/10/17 14:59:34
     */
    public Connection connect(final Endpoint endpoint, final long connectTimeoutMillis) {
        // 确保服务已启动
        acquireOK();

        final ChannelFuture future = bootstrap.connect(Endpoint.toSocketAddress(endpoint));
        if (future.awaitUninterruptibly(connectTimeoutMillis, TimeUnit.MILLISECONDS)) {
            final Channel channel = future.channel();
            if (channel.isActive()) {
                return new Connection(channel);
            } else {
                // connect failed
                final String errorMessage = String.format("Connect to %s failed", endpoint);
                log.error(errorMessage, future.cause());
                throw new ConnectFailedException(errorMessage, future.cause());
            }
        } else {
            // connect timeout
            final String errorMessage = String.format("Connect to %s timeout", endpoint);
            log.error(errorMessage);
            throw new ConnectTimeoutException(errorMessage);
        }
    }
}
