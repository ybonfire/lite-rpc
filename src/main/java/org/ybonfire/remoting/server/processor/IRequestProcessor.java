package org.ybonfire.remoting.server.processor;

import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;

import java.io.Serializable;

/**
 * 远程调用请求处理器接口
 *
 * @author yuanbo
 * @date 2022-12-15 17:33
 */
public interface IRequestProcessor<T extends Serializable> {

    /**
     * @description: 处理请求
     * @param:
     * @return:
     * @date: 2022/12/22 15:54:08
     */
    void process(final IRemotingRequest<T> request, final Connection connection);
}
