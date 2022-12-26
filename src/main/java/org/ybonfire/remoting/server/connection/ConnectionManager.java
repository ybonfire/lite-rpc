package org.ybonfire.remoting.server.connection;

import lombok.Data;
import org.ybonfire.remoting.common.concurrent.AbstractThreadService;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.server.RemotingServerOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端连接管理器
 *
 * @author yuanbo
 * @date 2022-12-23 10:01
 */
public class ConnectionManager extends AbstractLifeCycle {
    private final Map<String/*ConnectionId*/, ConnectionWrapper> connections = new ConcurrentHashMap<>();
    private final ConnectionManageService connectionManageService = new ConnectionManageService();
    private final RemotingServerOptions options;
    private final String serverId;

    public ConnectionManager(final RemotingServerOptions options, final String serverId) {
        this.options = options;
        this.serverId = serverId;
    }

    /**
     * @description: 添加连接
     * @param:
     * @return:
     * @date: 2022/12/23 10:12:09
     */
    public void add(final Connection connection) {
        connections.put(connection.getId(), new ConnectionWrapper(connection));
    }

    /**
     * @description: 获取连接
     * @param:
     * @return:
     * @date: 2022/12/23 10:12:45
     */
    public Optional<ConnectionWrapper> get(final String id) {
        return Optional.ofNullable(connections.get(id));
    }

    /**
     * @description: 启动服务
     * @param:
     * @return:
     * @date: 2022/12/23 10:23:57
     */
    @Override
    protected void onStart() {
        connectionManageService.start();
    }

    /**
     * @description: 关闭服务
     * @param:
     * @return:
     * @date: 2022/12/23 10:24:02
     */
    @Override
    protected void onShutdown() {
        connectionManageService.shutdown();
    }

    /**
     * @description: Connection包装类
     * @author: yuanbo
     * @date: 2022/12/23
     */
    @Data
    public static class ConnectionWrapper {
        private final Connection connection;
        /**
         * 上次接收到Heartbeat的时间戳
         */
        private volatile long lastReceiveHeartbeatTimestamp;

        private ConnectionWrapper(final Connection connection) {
            this.connection = connection;
            this.lastReceiveHeartbeatTimestamp = System.currentTimeMillis();
        }

        public void setLastReceiveHeartbeatTimestamp(final long lastReceiveHeartbeatTimestamp) {
            this.lastReceiveHeartbeatTimestamp = lastReceiveHeartbeatTimestamp;
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
            final Iterator<Map.Entry<String, ConnectionWrapper>> iterator = connections.entrySet().iterator();
            while (iterator.hasNext()) {
                final ConnectionWrapper connection = iterator.next().getValue();
                // 移除失效连接
                if (isSessionTimeout(connection)) {
                    if (connection.getConnection().close()) {
                        iterator.remove();
                    }
                }
            }
        }

        /**
         * @description: 判断会话是否超时：太久未接收到Heartbeat保活请求
         * @param:
         * @return:
         * @date: 2022/12/23 10:25:25
         */
        private boolean isSessionTimeout(final ConnectionWrapper connection) {
            return System.currentTimeMillis() - connection.getLastReceiveHeartbeatTimestamp() > options
                .getConnectionTTL();
        }
    }
}
