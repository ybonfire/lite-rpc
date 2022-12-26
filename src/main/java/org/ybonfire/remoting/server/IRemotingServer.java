package org.ybonfire.remoting.server;

import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.lifecycle.ILifeCycle;
import org.ybonfire.remoting.server.processor.IRequestProcessor;

/**
 * 远程服务器接口
 *
 * @author yuanbo
 * @date 2022-12-15 16:33
 */
public interface IRemotingServer extends ILifeCycle {

    /**
     * @description: 获取服务地址
     * @param:
     * @return:
     * @date: 2022/12/15 17:30:51
     */
    Endpoint getEndpoint();

    /**
     * @description: 注册请求处理器
     * @param:
     * @return:
     * @date: 2022/12/15 17:33:31
     */
    void registerProcessor(final RequestCodeEnum code, final IRequestProcessor processor);
}
