package org.ybonfire.remoting;

import org.apache.commons.lang3.ObjectUtils;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;
import org.ybonfire.remoting.protocol.response.RemotingResponse;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 远程调用响应构造类
 *
 * @author yuanbo
 * @date 2022-12-21 14:53
 */
public final class RemoteResponseBuilder {
    private static final String SUCCESS = "success";

    /**
     * @description: 构造响应
     * @param:
     * @return:
     * @date: 2022/12/21 15:11:51
     */
    public static <T extends Serializable> IRemotingResponse<T> response(final ResponseStatusEnum status,
        final String commandId, final String message, final T data) {
        return new RemotingResponse<>(status, commandId, message, data);
    }

    /**
     * @description: 构造成功响应
     * @param:
     * @return:
     * @date: 2022/12/21 14:55:06
     */
    public static <T extends Serializable> IRemotingResponse<T> success(final String commandId, final T data) {
        return response(ResponseStatusEnum.SUCCESS, commandId, SUCCESS, data);
    }

    /**
     * @description: 构造异常响应
     * @param:
     * @return:
     * @date: 2022/12/21 14:59:54
     */
    public static <T extends Serializable> IRemotingResponse<T> fail(final String commandId,
        final Supplier<Throwable> supplier) {
        final Throwable ex = ObjectUtils.defaultIfNull(supplier.get(), new UnknownError());
        return response(ResponseStatusEnum.fromException(ex.getClass()), commandId, ex.getMessage(), null);
    }

    private RemoteResponseBuilder() {}
}
