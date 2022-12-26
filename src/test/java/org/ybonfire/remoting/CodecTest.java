package org.ybonfire.remoting;

import org.junit.Assert;
import org.junit.Test;
import org.ybonfire.remoting.codec.serializer.IRemotingCommandSerializer;
import org.ybonfire.remoting.codec.serializer.hessian.HessianRemotingCommandSerializer;
import org.ybonfire.remoting.protocol.IRemotingCommand;
import org.ybonfire.remoting.protocol.request.RemotingRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * CodecTest
 *
 * @author yuanbo
 * @date 2022-12-23 17:50
 */
public class CodecTest {
    private static final IRemotingCommandSerializer SERIALIZER = new HessianRemotingCommandSerializer();
    private static final int PARALLEL = 100;

    @Test
    public void codec() {
        final IRemotingCommand<?> command = new RemotingRequest<>(RequestCodeEnum.TEST, null);
        final byte[] bytes = SERIALIZER.encode(command);
        final IRemotingCommand<?> result = SERIALIZER.decode(bytes);
        Assert.assertTrue(result instanceof RemotingRequest);
    }

    @Test
    public void parallelCodec() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(PARALLEL);
        final ExecutorService executorService = ForkJoinPool.commonPool();
        final List<IRemotingCommand<?>> commands = new CopyOnWriteArrayList<>();
        for (int i = 0; i < PARALLEL; ++i) {
            executorService.execute(() -> {
                try {
                    final IRemotingCommand<?> command = new RemotingRequest<>(RequestCodeEnum.TEST, null);
                    final byte[] bytes = SERIALIZER.encode(command);
                    final IRemotingCommand<?> result = SERIALIZER.decode(bytes);
                    commands.add(result);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        for (int i = 0; i < commands.size(); ++i) {
            Assert.assertTrue(commands.get(i) instanceof RemotingRequest);
        }
    }
}
