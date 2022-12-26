package org.ybonfire.remoting;

import org.junit.Assert;
import org.junit.Test;
import org.ybonfire.remoting.common.concurrent.SerializingExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SerializingExecutorTest
 *
 * @author yuanbo
 * @date 2022-12-19 16:42
 */
public class SerializingExecutorTest {
    private static final int PARALLEL = 1000;
    private static final SerializingExecutor SERIALIZING_EXECUTOR = new SerializingExecutor();

    static {
        SERIALIZING_EXECUTOR.start();
    }

    @Test
    public void serializingTest() {
        final List<Integer> result = new ArrayList<>(PARALLEL);
        final List<CompletableFuture<?>> futures = new ArrayList<>(PARALLEL);
        for (int i = 0; i < PARALLEL; ++i) {
            final int temp = i;
            futures.add(CompletableFuture.runAsync(() -> result.add(temp), SERIALIZING_EXECUTOR));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        for (int i = 0; i < result.size(); ++i) {
            Assert.assertTrue(result.get(i).equals(i));
        }
    }
}
