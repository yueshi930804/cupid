package com.yue.cupid.counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Yue
 * @since 2019/01/15
 * 计数器 - 长整型
 */
public class Counter4Long implements Counter {

    /**
     * 计数器映射
     */
    private Map<String, AtomicLong> counterMap;

    public Counter4Long() {
        this.counterMap = new ConcurrentHashMap<>();
    }

    /**
     * 增加
     *
     * @param name 计数器名称
     * @return 增加后计数
     */
    @Override
    public long increment(final String name) {
        return this.getCounter(name).incrementAndGet();
    }

    /**
     * 减少
     *
     * @param name 计数器名称
     * @return 减少后计数
     */
    @Override
    public long decrement(final String name) {
        return this.getCounter(name).decrementAndGet();
    }

    /**
     * 计数
     *
     * @param name   计数器名称
     * @param offset 偏移量
     * @return 偏移后计数
     */
    @Override
    public long count(final String name, final long offset) {
        return this.getCounter(name).addAndGet(offset);
    }

    /**
     * 重置
     *
     * @param name 计数器名称
     */
    @Override
    public void reset(final String name) {
        this.counterMap.computeIfPresent(name, (key, value) -> {
            value.set(0);
            return value;
        });
    }

    /**
     * 移除
     *
     * @param name 计数器名称
     */
    @Override
    public void remove(final String name) {
        this.counterMap.remove(name);
    }

    /**
     * 获取计数器
     *
     * @param name 计数器名称
     * @return 计数器
     */
    private AtomicLong getCounter(final String name) {
        AtomicLong counter = this.counterMap.get(name);
        if (counter == null) {
            counter = this.counterMap.computeIfAbsent(name, key -> new AtomicLong(0L));
        }
        return counter;
    }

}
