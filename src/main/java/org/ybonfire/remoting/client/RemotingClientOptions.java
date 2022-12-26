package org.ybonfire.remoting.client;

import lombok.Getter;

/**
 * 远程调用客户端配置
 *
 * @author yuanbo
 * @date 2022-12-19 10:42
 */
@Getter
public class RemotingClientOptions {
    /**
     * EventLoop线程数
     */
    private final int eventLoopThreadNums;
    /**
     * Worker线程数
     */
    private final int workerThreadNums;
    /**
     * Socket Send Buffer size;
     */
    private final int clientSocketSendBufferSize;
    /**
     * Socket receive buffer size
     */
    private final int clientSocketReceiveBufferSize;
    /**
     * Default Remote Invoke Timeout
     */
    private final long defaultRemoteInvokeTimeoutMillis;
    /**
     * Heartbeat send interval
     */
    private final long heartbeatIntervalMillis;

    private RemotingClientOptions(final Builder builder) {
        this.eventLoopThreadNums = builder.eventLoopThreadNums;
        this.workerThreadNums = builder.workerThreadNums;
        this.clientSocketSendBufferSize = builder.clientSocketSendBufferSize;
        this.clientSocketReceiveBufferSize = builder.clientSocketReceiveBufferSize;
        this.defaultRemoteInvokeTimeoutMillis = builder.defaultRemoteInvokeTimeoutMillis;
        this.heartbeatIntervalMillis = builder.heartbeatIntervalMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @description: RemotingServerOptions构造器
     * @author: yuanbo
     * @date: 2022/12/16
     */
    public static class Builder {
        /**
         * EventLoop线程数
         */
        private int eventLoopThreadNums = 1;
        /**
         * Worker线程数
         */
        private int workerThreadNums = 4;
        /**
         * Socket Send Buffer size;
         */
        private int clientSocketSendBufferSize = 65535;
        /**
         * Socket receive buffer size
         */
        private int clientSocketReceiveBufferSize = 65535;
        /**
         * Default Remote Invoke Timeout
         */
        private long defaultRemoteInvokeTimeoutMillis = 15 * 1000L;
        /**
         * Heartbeat send interval
         */
        private long heartbeatIntervalMillis = 30 * 1000L;

        public Builder eventLoopThreadNums(final int eventLoopThreadNums) {
            this.eventLoopThreadNums = eventLoopThreadNums;
            return this;
        }

        public Builder workerThreadNums(final int workerThreadNums) {
            this.workerThreadNums = workerThreadNums;
            return this;
        }

        public Builder clientSocketSendBufferSize(final int clientSocketSendBufferSize) {
            this.clientSocketSendBufferSize = clientSocketSendBufferSize;
            return this;
        }

        public Builder clientSocketReceiveBufferSize(final int clientSocketReceiveBufferSize) {
            this.clientSocketReceiveBufferSize = clientSocketReceiveBufferSize;
            return this;
        }

        public Builder defaultRemoteInvokeTimeoutMillis(final long defaultRemoteInvokeTimeoutMillis) {
            this.defaultRemoteInvokeTimeoutMillis = defaultRemoteInvokeTimeoutMillis;
            return this;
        }

        public Builder heartbeatIntervalMillis(final long heartbeatIntervalMillis) {
            this.heartbeatIntervalMillis = heartbeatIntervalMillis;
            return this;
        }

        public RemotingClientOptions build() {
            return new RemotingClientOptions(this);
        }

    }
}
