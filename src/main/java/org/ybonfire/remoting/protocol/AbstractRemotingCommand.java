package org.ybonfire.remoting.protocol;

import org.ybonfire.remoting.RemotingCommandTypeEnum;

import java.io.Serializable;

/**
 * 这里添加类的注释【强制】
 *
 * @author yuanbo
 * @date 2022-12-15 17:23
 */
public class AbstractRemotingCommand<T extends Serializable> implements IRemotingCommand<T> {
    private final RemotingCommandTypeEnum typeEnum;
    private final String commandId;
    private final T body;

    public AbstractRemotingCommand(final RemotingCommandTypeEnum typeEnum, final String commandId, final T body) {
        this.typeEnum = typeEnum;
        this.commandId = commandId;
        this.body = body;
    }

    @Override
    public RemotingCommandTypeEnum getType() {
        return typeEnum;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    public T getBody() {
        return body;
    }
}
