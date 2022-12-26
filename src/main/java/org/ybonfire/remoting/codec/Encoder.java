package org.ybonfire.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.ybonfire.remoting.codec.serializer.IRemotingCommandSerializer;
import org.ybonfire.remoting.codec.serializer.hessian.HessianRemotingCommandSerializer;
import org.ybonfire.remoting.protocol.IRemotingCommand;

/**
 * Netty序列化器
 *
 * @author yuanbo
 * @date 2022-12-19 14:41
 */
@Slf4j
@ChannelHandler.Sharable
public final class Encoder extends MessageToByteEncoder<IRemotingCommand<?>> {
    private final IRemotingCommandSerializer serializer;

    public Encoder() {
        this(new HessianRemotingCommandSerializer());
    }

    public Encoder(final IRemotingCommandSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * @description: 序列化
     * @param:
     * @return:
     * @date: 2022/12/19 14:43:35
     */
    @Override
    protected void encode(final ChannelHandlerContext ctx, final IRemotingCommand<?> msg, final ByteBuf out)
        throws Exception {
        final byte[] result = serializer.encode(msg);
        if (ArrayUtils.isNotEmpty(result)) {
            out.writeBytes(result);
        }
    }
}
