package org.ybonfire.remoting.callback;

import org.ybonfire.remoting.protocol.IRemotingCommand;

/**
 * 远程调用回调函数接口
 *
 * @author yuanbo
 * @date 2022-12-19 10:20
 */
public interface IRemotingInvokeCallback {

    /**
     * @description: 当收到远程调用响应时的执行流程
     * @param:
     * @return:
     * @date: 2022/12/19 10:21:05
     */
    void onResponse(final IRemotingCommand<?> response);
}
