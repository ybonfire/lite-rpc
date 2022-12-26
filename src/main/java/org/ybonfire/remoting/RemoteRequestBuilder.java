package org.ybonfire.remoting;

import org.ybonfire.remoting.protocol.request.HeartbeatRequest;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.request.RemotingRequest;

/**
 * 远程调用请求构造类
 *
 * @author yuanbo
 * @date 2022-12-21 18:31
 */
public final class RemoteRequestBuilder {

    /**
     * @description: 构造客户端心跳请求
     * @param:
     * @return:
     * @date: 2022/12/21 18:36:06
     */
    public static IRemotingRequest<HeartbeatRequest> buildClientHeartbeatRequest(final String clientId,
        final String connectionId, final long heartbeatIntervalMillis) {
        return new RemotingRequest<>(RequestCodeEnum.HEARTBEAT,
            new HeartbeatRequest(clientId, connectionId, heartbeatIntervalMillis));
    }

    private RemoteRequestBuilder() {}
}
