package org.ybonfire.remoting.util;

/**
 * 参数校验器
 *
 * @author yuanbo
 * @date 2022-12-15 16:15
 */
public final class PreCondition {

    /**
     * @description: 校验参数不为Null, 否则抛出{@link IllegalArgumentException}
     * @param:
     * @return:
     * @date: 2022/12/15 16:16:58
     */
    public static <T> T notNull(final T src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }

        return src;
    }

    /**
     * @description: 校验参数不为Null, 否则抛出{@link IllegalArgumentException}
     * @param:
     * @return:
     * @date: 2022/12/15 16:16:58
     */
    public static <T> T notNull(final T src, final String message) {
        if (src == null) {
            throw new IllegalArgumentException(message);
        }

        return src;
    }

    /**
     * @description: 校验表达式为True，否则抛出{@link IllegalStateException}
     * @param:
     * @return:
     * @date: 2022/12/21 10:07:47
     */
    public static void assertTrue(final boolean express, final String message) {
        if (!express) {
            throw new IllegalStateException(message);
        }
    }

    private PreCondition() {}
}
