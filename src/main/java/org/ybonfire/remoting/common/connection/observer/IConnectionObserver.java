package org.ybonfire.remoting.common.connection.observer;

import org.ybonfire.remoting.Ordered;
import org.ybonfire.remoting.common.connection.Connection;

/**
 * 连接观察者接口
 *
 * @author yuanbo
 * @date 2022-12-23 09:55
 */
public interface IConnectionObserver extends Ordered {

    /**
     * @description: 连接关闭回调
     * @param:
     * @return:
     * @date: 2022/12/23 09:55:25
     */
    void onClose(final Connection connection);
}
