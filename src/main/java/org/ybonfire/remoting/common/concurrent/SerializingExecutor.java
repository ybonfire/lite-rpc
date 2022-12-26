package org.ybonfire.remoting.common.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.ybonfire.remoting.lifecycle.AbstractLifeCycle;
import org.ybonfire.remoting.util.PreCondition;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * 串行化线程池
 *
 * @author yuanbo
 * @date 2022-12-16 15:39
 */
@Slf4j
public class SerializingExecutor extends AbstractLifeCycle implements Executor {
    private static final String DEFAULT_NAME = "SerializingExecutor";
    private final String name;
    private final BlockingQueue<Runnable> tasks;
    private final SerializingExecutorWorker worker = new SerializingExecutorWorker();
    private final IThreadServiceExecuteFailedCallback uncaughtExceptionHandler = new LogUncaughtExceptionHandler();
    private final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler();

    public SerializingExecutor() {
        this(DEFAULT_NAME, Integer.MAX_VALUE);
    }

    public SerializingExecutor(final String name) {
        this(name, Integer.MAX_VALUE);
    }

    public SerializingExecutor(final int capacity) {
        this(DEFAULT_NAME, capacity);
    }

    public SerializingExecutor(final String name, final int capacity) {
        this.name = name;
        this.tasks = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * @description: 执行线程任务
     * @param:
     * @return:
     * @date: 2022/12/16 15:41:55
     */
    @Override
    public void execute(final Runnable task) {
        doExecute(PreCondition.notNull(task));
    }

    /**
     * @description: 执行线程任务
     * @param:
     * @return:
     * @date: 2022/12/16 16:52:30
     */
    private void doExecute(final Runnable task) {
        acquireOK();
        synchronized (tasks) {
            if (tasks.remainingCapacity() > 0) {
                tasks.add(task);
            } else {
                rejectedExecutionHandler.rejectedExecution(task, this);
            }
        }
    }

    @Override
    protected void onStart() {
        worker.start();
    }

    @Override
    protected void onShutdown() {
        worker.shutdown();
    }

    /**
     * @description: 未捕获异常处理器
     * @author: yuanbo
     * @date: 2022/12/16
     */
    private static class LogUncaughtExceptionHandler implements IThreadServiceExecuteFailedCallback {

        @Override
        public void onException(final AbstractThreadService threadService, final Throwable e) {
            log.error("Uncaught exception in thread {}.", threadService.thread, e);
        }
    }

    /**
     * @description: 拒绝执行处理器
     * @author: yuanbo
     * @date: 2022/12/16
     */
    private static class RejectedExecutionHandler {

        /**
         * @description: 线程任务拒绝流程
         * @param:
         * @return:
         * @date: 2022/12/16 16:48:50
         */
        public void rejectedExecution(final Runnable r, final SerializingExecutor executor) {
            if (executor.isStarted()) {
                r.run();
            } else {
                throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + executor.name);
            }
        }
    }

    /**
     * @description: 串行化线程池Worker
     * @author: yuanbo
     * @date: 2022/12/16
     */
    private class SerializingExecutorWorker extends AbstractThreadService {

        public SerializingExecutorWorker() {
            super(uncaughtExceptionHandler, 0L);
        }

        /**
         * @description: 处理线程池任务
         * @param:
         * @return:
         * @date: 2022/12/16 15:47:54
         */
        @Override
        protected void execute() {
            while (isStarted()) {
                try {
                    final Runnable task = tasks.take();
                    try {
                        task.run();
                    } catch (Exception ex) {
                        if (uncaughtExceptionHandler != null) {
                            uncaughtExceptionHandler.onException(this, ex);
                        }
                    }
                } catch (InterruptedException ignored) {
                    // ignore
                }
            }
        }

        @Override
        protected String getName() {
            return "SerializingExecutorWorker";
        }
    }
}
