package org.ybonfire.remoting.protocol.response;

import org.ybonfire.remoting.RemotingCommandTypeEnum;
import org.ybonfire.remoting.ResponseStatusEnum;
import org.ybonfire.remoting.protocol.AbstractRemotingCommand;

import java.io.Serializable;

/**
 * 远程调用响应
 *
 * @author yuanbo
 * @date 2022-12-15 17:34
 */
public class RemotingResponse<T extends Serializable> extends AbstractRemotingCommand<T>
    implements IRemotingResponse<T> {
    private final Integer status;
    private final String message;

    public RemotingResponse(final ResponseStatusEnum status, final String commandId, final String message,
        final T body) {
        super(RemotingCommandTypeEnum.RESPONSE, commandId, body);
        this.status = status.getCode();
        this.message = message;
    }

    /**
     * @description: 获取响应码
     * @param:
     * @return:
     * @date: 2022/12/21 18:41:59
     */
    @Override
    public ResponseStatusEnum getStatus() {
        return ResponseStatusEnum.of(status);
    }

    /**
     * @description: 判断是否成功响应
     * @param:
     * @return:
     * @date: 2022/12/21 18:41:29
     */
    @Override
    public boolean isSuccess() {
        return status != null && status.equals(ResponseStatusEnum.SUCCESS.getCode());
    }

    /**
     * @description: 获取远程调用命令信息
     * @param:
     * @return:
     * @date: 2022/12/15 17:23:00
     */
    @Override
    public String getMessage() {
        return message;
    }
}
