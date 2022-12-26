package org.ybonfire.remoting.server;

import lombok.Getter;

/**
 * 远程调用服务端配置
 *
 * @author yuanbo
 * @date 2022-12-16 09:58
 */
@Getter
public class RemotingServerOptions {
    /**
     * Acceptor线程数
     */
    private final int acceptorThreadNums;
    /**
     * Selector线程数
     */
    private final int selectorThreadNums;
    /**
     * Worker线程数
     */
    private final int workerThreadNums;
    /**
     * Socket Send Buffer size;
     */
    private final int serverSocketSendBufferSize;
    /**
     * Socket receive buffer size
     */
    private final int serverSocketReceiveBufferSize;
    /**
     * Connection TTL
     */
    private final long connectionTTL;

    private RemotingServerOptions(final Builder builder) {
        this.acceptorThreadNums = builder.acceptorThreadNums;
        this.selectorThreadNums = builder.selectorThreadNums;
        this.workerThreadNums = builder.workerThreadNums;
        this.serverSocketSendBufferSize = builder.serverSocketSendBufferSize;
        this.serverSocketReceiveBufferSize = builder.serverSocketReceiveBufferSize;
        this.connectionTTL = builder.connectionTTL;
    }

    public int getAcceptorThreadNums() {
        return acceptorThreadNums;
    }

    public int getSelectorThreadNums() {
        return selectorThreadNums;
    }

    public int getWorkerThreadNums() {
        return workerThreadNums;
    }

    public int getServerSocketSendBufferSize() {
        return serverSocketSendBufferSize;
    }

    public int getServerSocketReceiveBufferSize() {
        return serverSocketReceiveBufferSize;
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
         * Acceptor线程数
         */
        private int acceptorThreadNums = 1;
        /**
         * Selector线程数
         */
        private int selectorThreadNums = 3;
        /**
         * Worker线程数
         */
        private int workerThreadNums = Runtime.getRuntime().availableProcessors();
        /**
         * Socket Send Buffer size;
         */
        private int serverSocketSendBufferSize = 65535;
        /**
         * Socket receive buffer size
         */
        private int serverSocketReceiveBufferSize = 65535;
        /**
         * Connection TTL
         */
        private long connectionTTL = 90 * 1000L;

        public Builder acceptorThreadNums(final int acceptorThreadNums) {
            this.acceptorThreadNums = acceptorThreadNums;
            return this;
        }

        public Builder selectorThreadNums(final int selectorThreadNums) {
            this.selectorThreadNums = selectorThreadNums;
            return this;
        }

        public Builder workerThreadNums(final int workerThreadNums) {
            this.workerThreadNums = workerThreadNums;
            return this;
        }

        public Builder serverSocketSendBufferSize(final int serverSocketSendBufferSize) {
            this.serverSocketSendBufferSize = serverSocketSendBufferSize;
            return this;
        }

        public Builder serverSocketReceiveBufferSize(final int serverSocketReceiveBufferSize) {
            this.serverSocketReceiveBufferSize = serverSocketReceiveBufferSize;
            return this;
        }

        public Builder connectionTTL(final long connectionTTL) {
            this.connectionTTL = connectionTTL;
            return this;
        }

        public RemotingServerOptions build() {
            return new RemotingServerOptions(this);
        }
    }
}
