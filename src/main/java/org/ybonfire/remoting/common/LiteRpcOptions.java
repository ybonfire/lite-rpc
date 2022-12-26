package org.ybonfire.remoting.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这里添加类的注释【强制】
 *
 * @author yuanbo
 * @date 2022-12-20 15:26
 */
public abstract class LiteRpcOptions {
    private Map<String, Object> options = new ConcurrentHashMap<>();

    protected <T> T get(final String key, final T defaultValue) {
        final Object value = options.get(key);
        return value == null ? defaultValue : (T)value;
    }

    protected <T> void set(final String key, final T value) {
        options.put(key, value);
    }
}
