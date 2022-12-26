package org.ybonfire.remoting.protocol;

import org.ybonfire.remoting.RemotingCommandTypeEnum;

import java.io.Serializable;

/**
 * 远程调用命令（请求/响应）接口
 *
 * @author yuanbo
 * @date 2022-12-15 16:37
 */
public interface IRemotingCommand<T extends Serializable> extends Serializable {

    /**
     * @description: 获取命令Id，对于一组对应的请求/响应，其Id相同
     * @param:
     * @return:
     * @date: 2022/12/15 16:59:02
     */
    String getCommandId();

    /**
     * @description: 获取远程调用命令数据
     * @param:
     * @return:
     * @date: 2022/12/15 17:23:09
     */
    T getBody();

    /**
     * @description: 获取远程调用命令类型 {@link RemotingCommandTypeEnum}
     * @param:
     * @return:
     * @date: 2022/12/15 17:01:49
     */
    RemotingCommandTypeEnum getType();
}
