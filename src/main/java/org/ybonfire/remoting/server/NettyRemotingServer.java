package org.ybonfire.remoting.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.codec.Decoder;
import org.ybonfire.remoting.codec.Encoder;
import org.ybonfire.remoting.exception.ServerStartupException;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.server.connection.ConnectionManager;
import org.ybonfire.remoting.server.handler.NettyRemotingRequestHandler;
import org.ybonfire.remoting.server.handler.ServerNettyConnectEventHandler;
import org.ybonfire.remoting.server.processor.IRequestProcessor;
import org.ybonfire.remoting.server.processor.RequestProcessorManager;
import org.ybonfire.remoting.util.NamedThreadFactory;
import org.ybonfire.remoting.util.NettyUtil;
import org.ybonfire.remoting.util.PreCondition;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于Netty的远程调用服务器
 *
 * @author yuanbo
 * @date 2022-12-16 09:36
 */
@Slf4j
public class NettyRemotingServer extends AbstractLifeCycle implements IRemotingServer {
    private final String id = UUID.randomUUID().toString();
    private final ServerBootstrap bootstrap = new ServerBootstrap();
    private final RequestProcessorManager requestProcessorManager = new RequestProcessorManager();
    private final Endpoint endpoint;
    private final RemotingServerOptions options;
    private final EventLoopGroup acceptorEventLoop;
    private final EventLoopGroup selectorEventLoop;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private final ConnectionManager connectionManager;
    private final ServerNettyConnectEventHandler nettyConnectEventHandler;

    public NettyRemotingServer(final int port, final RemotingServerOptions options) {
        PreCondition.assertTrue(port >= 0 && port <= 65535, "port must be between 0 and 65535.");
        PreCondition.notNull(options, "options");

        this.endpoint = Endpoint.of(new InetSocketAddress(port).getAddress().getHostAddress(), port);
        this.options = options;
        this.acceptorEventLoop =
            buildEventLoop(this.options.getAcceptorThreadNums(), buildThreadFactory("acceptor_event_loop_", true));
        this.selectorEventLoop =
            buildEventLoop(this.options.getSelectorThreadNums(), buildThreadFactory("selector_event_loop_", true));
        this.connectionManager = new ConnectionManager(this.options, this.id);
        this.nettyConnectEventHandler = new ServerNettyConnectEventHandler(this.connectionManager);
    }

    /**
     * @description: 启动流程
     * @param:
     * @return:
     * @date: 2022/12/16 09:37:54
     */
    @Override
    protected void onStart() {
        // connectionManager
        this.connectionManager.start();

        // nettyConnectEventHandler
        this.nettyConnectEventHandler.start();

        // executorGroup
        final int executorThreadNums = this.options.getWorkerThreadNums();
        this.defaultEventExecutorGroup = buildDefaultEventExecutorGroup(executorThreadNums, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("EventExecutor_%d", this.threadIndex.incrementAndGet()));
            }
        });

        // start server
        this.bootstrap.group(acceptorEventLoop, selectorEventLoop).channel(NettyUtil.getServerSocketChannelClass())
            .option(ChannelOption.SO_BACKLOG, 1024).option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_SNDBUF, options.getServerSocketSendBufferSize())
            .childOption(ChannelOption.SO_RCVBUF, options.getServerSocketReceiveBufferSize())
            .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.info(ch.remoteAddress().toString());
                    ch.pipeline().addLast(defaultEventExecutorGroup, new Decoder(), new Encoder(),
                        nettyConnectEventHandler, new NettyRemotingRequestHandler(requestProcessorManager));
                }
            });

        try {
            ChannelFuture future = this.bootstrap.bind(this.endpoint.getHost(), this.endpoint.getPort()).sync();
            log.info("Server startup success. localAddress:[{}]", future.channel().localAddress().toString());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerStartupException("this.bootstrap.bind().sync() InterruptedException", e);
        }
    }

    /**
     * @description: 关闭流程
     * @param:
     * @return:
     * @date: 2022/12/16 09:37:56
     */
    @Override
    protected void onShutdown() {
        // default executor group
        this.defaultEventExecutorGroup.shutdownGracefully();

        // selector event loop
        this.selectorEventLoop.shutdownGracefully();

        // acceptor event loop
        this.acceptorEventLoop.shutdownGracefully();

        // nettyConnectEventHandler
        this.nettyConnectEventHandler.shutdown();

        // connectionManager
        this.connectionManager.shutdown();
    }

    /**
     * @description: 获取服务器地址
     * @param:
     * @return:
     * @date: 2022/12/16 09:37:59
     */
    @Override
    public Endpoint getEndpoint() {
        return null;
    }

    /**
     * @description: 注册请求Processor
     * @param:
     * @return:
     * @date: 2022/12/16 09:38:01
     */
    @Override
    public void registerProcessor(final RequestCodeEnum code, final IRequestProcessor processor) {
        this.requestProcessorManager.register(code, processor);
    }

    /**
     * @description: 构造EventLoopGroup
     * @param:
     * @return:
     * @date: 2022/12/16 12:21:07
     */
    private EventLoopGroup buildEventLoop(final int theadNums, final ThreadFactory threadFactory) {
        return new NioEventLoopGroup(theadNums, threadFactory);
    }

    /**
     * @description: 构造DefaultEventExecutorGroup
     * @param:
     * @return:
     * @date: 2022/12/16 14:48:57
     */
    private DefaultEventExecutorGroup buildDefaultEventExecutorGroup(final int threadNums,
        final ThreadFactory threadFactory) {
        return new DefaultEventExecutorGroup(threadNums, threadFactory);
    }

    /**
     * @description: 构造ThreadFactory
     * @param:
     * @return:
     * @date: 2022/12/16 10:29:23
     */
    private NamedThreadFactory buildThreadFactory(final String prefix, final boolean isDaemon) {
        return new NamedThreadFactory(prefix, isDaemon);
    }
}
