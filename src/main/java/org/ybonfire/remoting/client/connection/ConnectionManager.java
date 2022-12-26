package org.ybonfire.remoting.client.connection;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.RemoteRequestBuilder;
import org.ybonfire.remoting.client.RemotingClientOptions;
import org.ybonfire.remoting.common.concurrent.AbstractThreadService;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.exception.ConnectFailedException;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.protocol.request.HeartbeatRequest;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.response.HeartbeatResponse;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;
import org.ybonfire.remoting.util.PreCondition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端连接管理器
 *
 * @author yuanbo
 * @date 2022-12-16 10:38
 */
@Slf4j
public class ConnectionManager extends AbstractLifeCycle {
    private final Lock connectLock = new ReentrantLock();
    private final Map<Endpoint/*remote*/, ConnectionWrapper> connections = new HashMap<>();
    private final ConnectionManageService connectionManageService = new ConnectionManageService();
    private final RemotingClientOptions options;
    private final String clientId;
    private final ConnectionFactory connectionFactory;

    public ConnectionManager(final RemotingClientOptions options, final String clientId) {
        this.options = options;
        this.clientId = clientId;
        this.connectionFactory = new ConnectionFactory(this.options);
    }

    /**
     * @description: 获取指定目标地址的连接
     * @param:
     * @return:
     * @date: 2022/12/20 14:27:24
     */
    public Connection getOrCreate(final Endpoint endpoint, final long connectTimeoutMillis) {
        acquireOK();

        PreCondition.notNull(endpoint, "endpoint");
        PreCondition.assertTrue(connectTimeoutMillis >= 0L, "connectionTimeoutMillis must be greater than 0L");

        try {
            final long startTime = System.currentTimeMillis();
            if (connectLock.tryLock(connectTimeoutMillis, TimeUnit.MILLISECONDS)) { // TODO EndPoint粒度锁
                try {
                    // 获取到锁之后的剩余时间
                    final long remainingTime = connectTimeoutMillis - (System.currentTimeMillis() - startTime);
                    return tryToGet(endpoint).orElseGet(() -> create(endpoint, remainingTime));
                } finally {
                    connectLock.unlock();
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            // ignore
        }

        final String errorMessage = String.format("Connect to %s failed", endpoint);
        log.error(errorMessage);
        throw new ConnectFailedException(errorMessage);
    }

    /**
     * @description: 启动服务
     * @param:
     * @return:
     * @date: 2022/12/20 14:24:34
     */
    @Override
    protected void onStart() {
        connectionFactory.start();
        connectionManageService.start();
    }

    /**
     * @description: 关闭服务
     * @param:
     * @return:
     * @date: 2022/12/20 14:24:38
     */
    @Override
    protected void onShutdown() {
        connectionManageService.shutdown();
        connectionFactory.shutdown();
    }

    /**
     * @description: 尝试从连接池获取连接
     * @param:
     * @return:
     * @date: 2022/12/21 10:44:47
     */
    private Optional<Connection> tryToGet(final Endpoint endpoint) {
        if (connections.containsKey(endpoint)) {
            final Connection connection = connections.get(endpoint).getConnection();
            if (!connection.isOK()) {
                connections.remove(endpoint);
            } else {
                return Optional.of(connection);
            }
        }

        return Optional.empty();
    }

    /**
     * @description: 创建新连接并加入连接池
     * @param:
     * @return:
     * @date: 2022/12/21 10:45:22
     */
    private Connection create(final Endpoint endpoint, final long connectTimeoutMillis) {
        final Connection connection = connectionFactory.connect(endpoint, connectTimeoutMillis);
        connections.put(endpoint, new ConnectionWrapper(connection));
        return connection;
    }

    /**
     * @description: Connection包装类
     * @author: yuanbo
     * @date: 2022/12/23
     */
    @Data
    private static class ConnectionWrapper {
        private final Connection connection;
        /**
         * 上次发送Heartbeat的时间戳
         */
        private volatile long lastSendHeartbeatTimestamp;

        private ConnectionWrapper(final Connection connection) {
            this.connection = connection;
            this.lastSendHeartbeatTimestamp = -1L;
        }

    }

    /**
     * @description: 连接管理服务
     * @author: yuanbo
     * @date: 2022/12/21
     */
    private class ConnectionManageService extends AbstractThreadService {
        private static final String NAME = "ConnectionManageService";

        public ConnectionManageService() {
            super(50L);
        }

        @Override
        protected String getName() {
            return NAME;
        }

        @Override
        protected void execute() {
            final Iterator<Map.Entry<Endpoint, ConnectionWrapper>> iterator = connections.entrySet().iterator();
            while (iterator.hasNext()) {
                final ConnectionWrapper connection = iterator.next().getValue();
                // 移除失活的连接
                if (!connection.getConnection().isOK()) {
                    if (connection.getConnection().close()) {
                        iterator.remove();
                    }
                } else {
                    // 发送心跳
                    tryToTriggerHeartbeat(connection);
                }
            }
        }

        /**
         * @description: 尝试发送心跳请求
         * @param:
         * @return:
         * @date: 2022/12/21 10:54:45
         */
        private void tryToTriggerHeartbeat(final ConnectionWrapper connection) {
            if (isHeartbeatTimesUp(connection)) {
                heartbeat(connection);
            }
        }

        /**
         * @description: 判断是否到达心跳发送时间
         * @param:
         * @return:
         * @date: 2022/12/21 10:58:44
         */
        private boolean isHeartbeatTimesUp(final ConnectionWrapper connection) {
            return System.currentTimeMillis() - connection.getLastSendHeartbeatTimestamp() > options
                .getHeartbeatIntervalMillis();
        }

        /**
         * @description: 发送心跳
         * @param:
         * @return:
         * @date: 2022/12/21 10:59:56
         */
        private void heartbeat(final ConnectionWrapper wrapper) {
            final Connection connection = wrapper.getConnection();
            final IRemotingRequest<HeartbeatRequest> request = RemoteRequestBuilder
                .buildClientHeartbeatRequest(clientId, connection.getId(), options.getHeartbeatIntervalMillis());
            connection.invokeAsync(request, r -> {
                if (((IRemotingResponse<?>)r).isSuccess()) {
                    final HeartbeatResponse response = (HeartbeatResponse)r.getBody();
                    wrapper.setLastSendHeartbeatTimestamp(response.getReceiveTimestamp());
                } else {
                    // TODO 心跳响应异常处理
                }
            }, options.getDefaultRemoteInvokeTimeoutMillis());
        }
    }
}
