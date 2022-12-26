package org.ybonfire.remoting.codec.serializer.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.ybonfire.remoting.codec.serializer.IRemotingCommandSerializer;
import org.ybonfire.remoting.exception.CodecException;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.util.PreCondition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于Hessian序列化策略的远程调用命令序列化器
 *
 * @author yuanbo
 * @date 2022-12-19 14:44
 */
public class HessianRemotingCommandSerializer implements IRemotingCommandSerializer {
    private static final SerializerFactory SERIALIZER_FACTORY = new SerializerFactory();
    private static final ThreadLocal<ByteArrayOutputStream> BYTE_ARRAY_HOLDER =
        ThreadLocal.withInitial(ByteArrayOutputStream::new);

    /**
     * @description: 序列化
     * @param:
     * @return:
     * @date: 2022/12/19 14:45:49
     */
    @Override
    public byte[] encode(final IRemotingCommand<?> command) {
        PreCondition.notNull(command, "command");
        final ByteArrayOutputStream byteArray = BYTE_ARRAY_HOLDER.get();
        byteArray.reset();

        final Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(SERIALIZER_FACTORY);
        try {
            output.writeObject(command);
            output.close();
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer encode!", e);
        }

        return byteArray.toByteArray();
    }

    /**
     * @description: 反序列化
     * @param:
     * @return:
     * @date: 2022/12/19 14:45:51
     */
    @Override
    public <T extends IRemotingCommand<?>> T decode(final byte[] bytes) {
        PreCondition.notNull(bytes, "bytes");

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        input.setSerializerFactory(SERIALIZER_FACTORY);
        Object resultObject;
        try {
            resultObject = input.readObject();
            input.close();
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer decode!", e);
        }
        return (T)resultObject;
    }
}
