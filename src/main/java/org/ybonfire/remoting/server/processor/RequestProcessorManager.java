package org.ybonfire.remoting.server.processor;

import org.ybonfire.remoting.RequestCodeEnum;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求处理器管理类
 *
 * @author yuanbo
 * @date 2022-12-16 11:12
 */
public class RequestProcessorManager {
    private final Map<RequestCodeEnum, IRequestProcessor> processors = new ConcurrentHashMap<>();

    /**
     * @description: 注册对应RequestCode的Processor
     * @param:
     * @return:
     * @date: 2022/12/16 11:14:48
     */
    public void register(final RequestCodeEnum code, final IRequestProcessor processor) {
        processors.putIfAbsent(code, processor);
    }

    /**
     * @description: 获取对应RequestCode的Processor
     * @param:
     * @return:
     * @date: 2022/12/16 11:15:49
     */
    public Optional<IRequestProcessor> get(final RequestCodeEnum code) {
        return Optional.ofNullable(processors.get(code));
    }
}
