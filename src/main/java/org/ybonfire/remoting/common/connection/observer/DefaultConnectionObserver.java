package org.ybonfire.remoting.common.connection.observer;

import org.ybonfire.remoting.RemoteInvokeFuture;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.exception.IllegalConnectionStateException;

import java.util.List;

/**
 * 连接观察者
 *
 * @author yuanbo
 * @date 2022-12-22 10:16
 */
public class DefaultConnectionObserver implements IConnectionObserver {
    private static final IConnectionObserver INSTANCE = new DefaultConnectionObserver();

    private DefaultConnectionObserver() {}

    /**
     * @description: 连接关闭回调
     * @param:
     * @return:
     * @date: 2022/12/15 16:23:53
     */
    @Override
    public void onClose(final Connection connection) {
        final List<RemoteInvokeFuture> futures = connection.getAllRemoteInvokeFutures();
        for (final RemoteInvokeFuture future : futures) {
            connection.removeRemoteInvokeFuture(future.getRequestId());
            future.completeWithException(IllegalConnectionStateException::new);
        }
    }

    /**
     * @description: 获取单例
     * @param:
     * @return:
     * @date: 2022/12/22 10:26:00
     */
    public static IConnectionObserver newInstance() {
        return INSTANCE;
    }
}
