package org.ybonfire.remoting.server.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.RemoteResponseBuilder;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.exception.IllegalConnectionStateException;
import org.ybonfire.remoting.protocol.request.HeartbeatRequest;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.response.HeartbeatResponse;
import org.ybonfire.remoting.server.connection.ConnectionManager;
import org.ybonfire.remoting.server.processor.IRequestProcessor;

import java.util.Optional;

/**
 * 客户端心跳请求处理器
 *
 * @author yuanbo
 * @date 2022-12-22 16:48
 */
@Slf4j
public class HeartbeatRequestProcessor implements IRequestProcessor<HeartbeatRequest> {
    private final ConnectionManager connectionManager;

    public HeartbeatRequestProcessor(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * @description: 处理请求
     * @param:
     * @return:
     * @date: 2022/12/22 15:54:08
     */
    @Override
    public void process(final IRemotingRequest<HeartbeatRequest> request, final Connection connection) {
        final String requestId = request.getCommandId();
        final HeartbeatRequest data = request.getBody();
        final String connectionId = data.getConnectionId();
        final Optional<ConnectionManager.ConnectionWrapper> connectionOptional =
            connectionManager.get(data.getConnectionId());
        if (connectionOptional.isPresent()) {
            final ConnectionManager.ConnectionWrapper connectionWrapper = connectionOptional.get();
            final long now = System.currentTimeMillis();
            connectionWrapper.setLastReceiveHeartbeatTimestamp(now);
            connection.invokeOneway(RemoteResponseBuilder.success(requestId, new HeartbeatResponse(connectionId, now)));
        } else {
            throw new IllegalConnectionStateException();
        }
    }
}
