package org.ybonfire.remoting.protocol.request;

import org.ybonfire.remoting.RemotingCommandTypeEnum;
import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.protocol.AbstractRemotingCommand;

import java.io.Serializable;
import java.util.UUID;

/**
 * 远程调用请求
 *
 * @author yuanbo
 * @date 2022-12-15 17:13
 */
public class RemotingRequest<T extends Serializable> extends AbstractRemotingCommand<T> implements IRemotingRequest<T> {
    private final Integer code;

    public RemotingRequest(final RequestCodeEnum code, final T body) {
        super(RemotingCommandTypeEnum.REQUEST, UUID.randomUUID().toString(), body);
        this.code = code.getCode();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
