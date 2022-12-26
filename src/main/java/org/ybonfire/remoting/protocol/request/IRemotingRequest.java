package org.ybonfire.remoting.protocol.request;

import org.ybonfire.remoting.protocol.IRemotingCommand;

import java.io.Serializable;

/**
 * 远程调用请求接口
 *
 * @author yuanbo
 * @date 2022-12-15 17:02
 */
public interface IRemotingRequest<T extends Serializable> extends IRemotingCommand<T> {

    /**
     * @description: 获取远程调用请求码，服务端通过请求码获取获取对应处理器
     * @param:
     * @return:
     * @date: 2022/12/15 17:03:13
     */
    Integer getCode();
}
