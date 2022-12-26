package org.ybonfire.remoting.common.connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.RemoteInvokeFuture;
import org.ybonfire.remoting.RemoteResponseBuilder;
import org.ybonfire.remoting.ResponseStatusEnum;
import org.ybonfire.remoting.callback.IRemotingInvokeCallback;
import org.ybonfire.remoting.common.connection.observer.DefaultConnectionObserver;
import org.ybonfire.remoting.common.connection.observer.IConnectionObserver;
import org.ybonfire.remoting.exception.IllegalConnectionStateException;
import org.ybonfire.remoting.exception.ReadTimeoutException;
import org.ybonfire.remoting.exception.RemotingInvokeExecuteException;
import org.ybonfire.remoting.exception.RemotingInvokeInterruptedException;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.util.PreCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客户端连接对象
 *
 * @author yuanbo
 * @date 2022-12-15 16:08
 */
@Slf4j
public class Connection {
    private static final AttributeKey<Connection> CONNECTION_KEY = AttributeKey.valueOf("connection");
    /**
     * 连接id，唯一标识
     */
    private final String id;
    /**
     * 本地地址
     */
    private final Endpoint local;
    /**
     * 远程地址
     */
    private final Endpoint remote;
    /**
     * Netty连接
     */
    private final Channel channel;
    /**
     * 在途的远程请求
     */
    private final Map<String/*commandId*/, RemoteInvokeFuture> futureTable = new ConcurrentHashMap<>();
    /**
     * 连接观察者
     */
    private final List<IConnectionObserver> observers = new CopyOnWriteArrayList<>();
    /**
     * 连接关闭标记
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public Connection(final Channel channel) {
        this.local = Endpoint.from(channel.localAddress());
        this.remote = Endpoint.from(channel.remoteAddress());
        this.channel = channel;
        this.channel.attr(CONNECTION_KEY).set(this);
        this.id = channel.id().asLongText();
        registerObserver(DefaultConnectionObserver.newInstance());
    }

    /**
     * @description: 同步调用
     * @param:
     * @return:
     * @date: 2022/12/21 14:31:50
     */
    public IRemotingCommand<?> invoke(final IRemotingCommand<?> request, final long timeoutMillis) {
        final long startTime = System.currentTimeMillis();

        final String requestId = request.getCommandId();
        final RemoteInvokeFuture future = build(requestId, null, timeoutMillis);

        // 缓存在途请求
        futureTable.putIfAbsent(requestId, future);

        try {
            // 校验连接状态
            if (!isOK()) {
                return RemoteResponseBuilder.fail(requestId, IllegalConnectionStateException::new);
            }

            // 校验剩余时间
            if (future.isTimeout()) {
                return RemoteResponseBuilder.fail(requestId, ReadTimeoutException::new);
            }

            // 发送请求
            this.channel.write(request).addListener(f -> {
                if (f.isSuccess()) {
                    future.onLaunchSuccess();
                } else {
                    log.error("Failed to send request. id:[{}]. ex:{}", requestId, f.cause());
                    future.completeWithException(() -> new RemotingInvokeExecuteException(f.cause()));
                }
            });

            // 等待响应
            try {
                final long remainingTime = timeoutMillis - (System.currentTimeMillis() - startTime);
                final IRemotingCommand<?> response = future.waitResponse(remainingTime);
                if (response == null) {
                    return RemoteResponseBuilder.response(ResponseStatusEnum.RESPONSE_READ_TIMEOUT, requestId, null,
                        null);
                }

                return response;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("doRequestSync interrupted.");
                throw new RemotingInvokeInterruptedException(ex);
            }
        } finally {
            // 移除在途请求
            futureTable.remove(requestId);
        }
    }

    /**
     * @description: 异步调用 with Callback
     * @param:
     * @return:
     * @date: 2022/12/21 14:31:55
     */
    public void invokeAsync(final IRemotingCommand<?> request, final IRemotingInvokeCallback callback,
        final long timeoutMillis) {
        final String requestId = request.getCommandId();
        final RemoteInvokeFuture future = build(requestId, callback, timeoutMillis);

        // 缓存在途请求
        futureTable.putIfAbsent(requestId, future);

        try {
            // 校验连接状态
            if (!isOK()) {
                future.completeWithException(IllegalConnectionStateException::new);
            }

            // 校验剩余时间
            if (future.isTimeout()) {
                future.completeWithException(ReadTimeoutException::new);
            }

            // 发送请求
            this.channel.write(request).addListener(f -> {
                if (f.isSuccess()) {
                    future.onLaunchSuccess();
                } else {
                    log.error("Failed to send request. id:[{}]. ex:{}", requestId, f.cause());
                    future.completeWithException(() -> new RemotingInvokeExecuteException(f.cause()));
                }
            });
        } finally {
            // 移除在途请求
            futureTable.remove(requestId);
        }
    }

    /**
     * @description: 异步调用 with CompletableFuture
     * @param:
     * @return:
     * @date: 2022/12/21 14:32:05
     */
    public CompletableFuture<IRemotingCommand<?>> invokeAsync(final IRemotingCommand<?> request,
        final long timeoutMillis) {
        final String requestId = request.getCommandId();
        final RemoteInvokeFuture future = build(requestId, null, timeoutMillis);

        // 缓存在途请求
        futureTable.putIfAbsent(requestId, future);

        try {
            // 校验连接状态
            if (!isOK()) {
                future.completeWithException(IllegalConnectionStateException::new);
                return future.getResponseFuture();
            }

            // 校验剩余时间
            if (future.isTimeout()) {
                future.completeWithException(ReadTimeoutException::new);
                return future.getResponseFuture();
            }

            // 发送请求
            this.channel.write(request).addListener(f -> {
                if (f.isSuccess()) {
                    future.onLaunchSuccess();
                } else {
                    log.error("Failed to send request. id:[{}]. ex:{}", requestId, f.cause());
                    future.completeWithException(() -> new RemotingInvokeExecuteException(f.cause()));
                }
            });

            return future.getResponseFuture();
        } finally {
            // 移除在途请求
            futureTable.remove(requestId);
        }
    }

    /**
     * @description: 单向调用
     * @param:
     * @return:
     * @date: 2022/12/21 14:32:19
     */
    public void invokeOneway(final IRemotingCommand<?> request) {
        this.channel.writeAndFlush(request);
    }

    /**
     * @description: 获取远程连接地址
     * @param:
     * @return:
     * @date: 2022/12/15 15:50:34
     */
    public Endpoint getRemoteEndpoint() {
        return remote;
    }

    /**
     * @description: 获取本地地址
     * @param:
     * @return:
     * @date: 2022/12/15 15:50:36
     */
    public Endpoint getLocalEndpoint() {
        return local;
    }

    /**
     * @description: 判断连接是否活跃
     * @param:
     * @return:
     * @date: 2022/12/15 16:07:26
     */
    public boolean isOK() {
        return channel != null && channel.isActive();
    }

    /**
     * @description: 注册连接观察者
     * @param:
     * @return:
     * @date: 2022/12/15 16:01:28
     */
    public void registerObserver(final IConnectionObserver observer) {
        PreCondition.notNull(observer, "observer");
        observers.add(observer);
        if (observers.size() > 1) {
            observers.sort(Comparator.comparing(IConnectionObserver::order));
        }
    }

    /**
     * @description: 尝试根据CommandId获取RemoteInvokeFuture
     * @param:
     * @return:
     * @date: 2022/12/21 22:10:47
     */
    public Optional<RemoteInvokeFuture> tryToGetRemoteInvokeFuture(final String commandId) {
        return Optional.ofNullable(futureTable.get(commandId));
    }

    /**
     * @description: 根据CommandId删除RemoteInvokeFuture
     * @param:
     * @return:
     * @date: 2022/12/21 22:11:40
     */
    public void removeRemoteInvokeFuture(final String commandId) {
        futureTable.remove(commandId);
    }

    /**
     * @description: 获取所有RemoteInvokeFuture
     * @param:
     * @return:
     * @date: 2022/12/22 10:21:06
     */
    public List<RemoteInvokeFuture> getAllRemoteInvokeFutures() {
        return new ArrayList<>(futureTable.values());
    }

    public String getId() {
        return id;
    }

    /**
     * @description: 关闭连接
     * @param:
     * @return:
     * @date: 2022/12/22 09:58:49
     */
    public boolean close() {
        if (isClosed.compareAndSet(false, true)) {
            if (channel != null) {
                channel.close().addListener(f -> {
                    if (f.isSuccess()) {
                        log.info("关闭连接成功. Connection:[{}]", this);
                        for (IConnectionObserver observer : observers) {
                            observer.onClose(this);
                        }
                    } else {
                        log.warn("关闭连接失败. Connection:[{}]", this);
                        isClosed.compareAndSet(true, false);
                    }
                });
            }
        }

        return isClosed.get();
    }

    /**
     * @description: 获取Netty Channel中的Connection对象
     * @param:
     * @return:
     * @date: 2022/12/22 13:50:37
     */
    public static Connection getFromChannel(final Channel channel) {
        final Connection connection = channel.attr(CONNECTION_KEY).get();
        return connection == null ? new Connection(channel) : connection;
    }

    @Override
    public String toString() {
        return "Connection{" + "id='" + id + '\'' + ", local=" + local + ", remote=" + remote + ", channel=" + channel
            + ", futureTable=" + futureTable + ", observers=" + observers + ", isClosed=" + isClosed + '}';
    }

    /**
     * @description: 构造RemoteInvokeFuture
     * @param:
     * @return:
     * @date: 2022/12/21 14:25:42
     */
    private RemoteInvokeFuture build(final String requestId, final IRemotingInvokeCallback callback,
        final long timeoutMillis) {
        final long timeoutTimestamp = System.currentTimeMillis() + timeoutMillis;
        return new RemoteInvokeFuture(requestId, callback, timeoutTimestamp);
    }
}
