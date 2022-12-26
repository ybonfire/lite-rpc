package org.ybonfire.remoting.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.codec.serializer.IRemotingCommandSerializer;
import org.ybonfire.remoting.codec.serializer.hessian.HessianRemotingCommandSerializer;

/**
 * Netty反序列化器
 *
 * @author yuanbo
 * @date 2022-12-19 14:41
 */
@Slf4j
public final class Decoder extends LengthFieldBasedFrameDecoder {
    private static final int INT_BYTE_LENGTH = 4;
    private final IRemotingCommandSerializer serializer;

    public Decoder() {
        this(new HessianRemotingCommandSerializer());
    }

    public Decoder(final IRemotingCommandSerializer serializer) {
        super(65536, 0, INT_BYTE_LENGTH, 0, 0);
        this.serializer = serializer;
    }

    /**
     * @description: 反序列化
     * @param:
     * @return:
     * @date: 2022/12/19 17:21:27
     */
    @Override
    protected Object decode(final ChannelHandlerContext ctx, final ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf)super.decode(ctx, in);
            if (frame == null) {
                return null;
            }

            if (frame.readableBytes() < INT_BYTE_LENGTH) {
                log.warn("数据异常, 丢弃");
                return null;
            }

            final int totalLength = frame.readInt();
            if (frame.readableBytes() < totalLength) {
                in.resetReaderIndex();
                log.warn("数据异常, 丢弃");
                return null;
            }

            return serializer.decode(frame.nioBuffer().array());
        } finally {
            if (frame != null) {
                frame.release();
            }
        }
    }
}
