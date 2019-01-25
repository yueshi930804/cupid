package com.yue.cupid.counter;

/**
 * @author Yue
 * @since 2019/01/23
 * 计数器
 */
public interface Counter {

    /**
     * 增加
     *
     * @param name 计数器名称
     * @return 增加后计数
     */
    long increment(String name);

    /**
     * 减少
     *
     * @param name 计数器名称
     * @return 减少后计数
     */
    long decrement(String name);

    /**
     * 计数
     *
     * @param name   计数器名称
     * @param offset 偏移量
     * @return 偏移后计数
     */
    long count(String name, long offset);

    /**
     * 重置
     *
     * @param name 计数器名称
     */
    void reset(String name);

    /**
     * 移除
     *
     * @param name 计数器名称
     */
    void remove(String name);
}
