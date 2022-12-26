package org.ybonfire.remoting;

/**
 * 对象优先级接口
 *
 * @author yuanbo
 * @date 2022-12-22 10:31
 */
public interface Ordered {
    /**
     * 最高优先级
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * 最低优先级
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * @description: 获取对象优先级
     * @param:
     * @return:
     * @date: 2022/12/22 10:32:45
     */
    default int order() {
        return LOWEST_PRECEDENCE;
    }
}
