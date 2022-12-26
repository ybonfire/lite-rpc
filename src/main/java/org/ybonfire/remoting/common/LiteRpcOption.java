package org.ybonfire.remoting.common;

import lombok.Data;

/**
 * 这里添加类的注释【强制】
 *
 * @author yuanbo
 * @date 2022-12-20 16:06
 */
@Data
public class LiteRpcOption<T> {
    private final String config;
    private final T defaultValue;

    public LiteRpcOption(final String config, final T defaultValue) {
        this.config = config;
        this.defaultValue = defaultValue;
    }
}
