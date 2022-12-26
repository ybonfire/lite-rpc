package org.ybonfire.remoting.protocol.response;

import org.ybonfire.remoting.ResponseStatusEnum;
import org.ybonfire.remoting.protocol.IRemotingCommand;

import java.io.Serializable;

/**
 * 远程调用响应接口
 *
 * @author yuanbo
 * @date 2022-12-15 17:08
 */
public interface IRemotingResponse<T extends Serializable> extends IRemotingCommand<T> {

    /**
     * @description: 获取响应码
     * @param:
     * @return:
     * @date: 2022/12/15 17:12:36
     */
    ResponseStatusEnum getStatus();

    /**
     * @description: 判断是否成功响应
     * @param:
     * @return:
     * @date: 2022/12/21 18:41:29
     */
    boolean isSuccess();

    /**
     * @description: 获取远程调用命令信息
     * @param:
     * @return:
     * @date: 2022/12/15 17:23:00
     */
    String getMessage();
}
