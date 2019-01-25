package com.yue.cupid.util;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yue
 * @since 2018/02/07
 * 日期工具类
 */
public class DateUtil {
    /**
     * 格式化构造器隐射 - key : 模式
     */
    private static final Map<String, DateTimeFormatter> FORMATTER_MAP = new ConcurrentHashMap<>();

    /**
     * 不可被实例化
     */
    private DateUtil() {
    }

    /**
     * 格式化
     *
     * @param temporal 时间访问器
     * @param pattern  模式
     * @return 格式化字符串
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        return getFormatter(pattern).format(temporal);
    }

    /**
     * 获取格式化构造器
     *
     * @param pattern 模式
     * @return 格式化构造器
     */
    private static DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter formatter = FORMATTER_MAP.get(pattern);
        if(formatter == null){
            formatter = FORMATTER_MAP.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
        }
        return formatter;
    }
}
