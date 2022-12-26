package org.ybonfire.remoting.codec.serializer;

import org.ybonfire.remoting.protocol.IRemotingCommand;

/**
 * 远程调用命令序列化接口
 *
 * @author yuanbo
 * @date 2022-12-19 14:44
 */
public interface IRemotingCommandSerializer {

    /**
     * @description: 序列化
     * @param:
     * @return:
     * @date: 2022/12/19 14:45:49
     */
    byte[] encode(final IRemotingCommand<?> command);

    /**
     * @description: 反序列化
     * @param:
     * @return:
     * @date: 2022/12/19 14:45:51
     */
    <T extends IRemotingCommand<?>> T decode(final byte[] bytes);
}
