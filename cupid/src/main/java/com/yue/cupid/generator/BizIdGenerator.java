package com.yue.cupid.generator;

import com.yue.cupid.counter.Counter;
import com.yue.cupid.counter.Counter4Integer;
import com.yue.cupid.counter.Counter4Long;
import com.yue.cupid.exception.BizIdException;
import com.yue.cupid.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yue
 * @since 2019/01/15
 * 业务ID生成器
 */
@Slf4j
public class BizIdGenerator {

    private static final String LOG_PREFIX = "[BizIdGenerator]";

    /**
     * 机器码
     */
    private String machineCode;
    /**
     * 计数器所占位数（默认8位）
     */
    private int counterBits = 8;
    /**
     * 计数器
     */
    private Counter counter;
    /**
     * 最大计数
     */
    private long maxCount;
    /**
     * 计数格式
     */
    private String countFormat;
    /**
     * 重置级别（默认按日重置）
     */
    private Level resetLevel = Level.DAY;
    /**
     * 日期级别（默认输出到秒）
     */
    private Level dateLevel = Level.SECOND;
    /**
     * 业务计数器映射
     */
    private Map<String, BizId> bizCounterM;

    public BizIdGenerator(String machineCode) {
        this.init(machineCode);
    }

    public BizIdGenerator(String machineCode, int counterBits, Level resetLevel,
                          Level dateLevel) {
        if (counterBits < 1 || counterBits > 18) {
            throw new BizIdException("Counter bits can't be less than 1 or greater than 18");
        }

        this.counterBits = counterBits;
        this.resetLevel = resetLevel;
        this.dateLevel = dateLevel;
        this.init(machineCode);
    }

    /**
     * 获取下一个业务ID
     *
     * @param bizMark 业务标识
     * @return 获取下一个业务ID
     */
    public String getNextId(String bizMark) {
        if (StringUtils.isBlank(bizMark)) {
            throw new BizIdException("Biz mark can't be blank");
        }
        BizId bizId = this.bizCounterM.get(bizMark);
        if (bizId == null) {
            bizId = this.bizCounterM.computeIfAbsent(bizMark, BizId::new);
        }
        String id = bizId.getNextId();
        log.debug("{} Next id - {}", LOG_PREFIX, id);
        return id;
    }

    /**
     * 初始化
     *
     * @param machineCode 机器码
     */
    private void init(String machineCode) {
        this.machineCode = machineCode;
        this.counter = this.getCounter();
        this.maxCount = this.getMaxCount();
        this.countFormat = this.getCountFormat();
        this.bizCounterM = new ConcurrentHashMap<>();
    }

    /**
     * 获取计数器，默认使用计数器 - 整型
     *
     * @return 计数器
     */
    private Counter getCounter() {
        if (this.counterBits <= 9) {
            return new Counter4Integer();
        }
        return new Counter4Long();
    }

    /**
     * 获取支持的最大计数，默认结果为9999999
     *
     * @return 最大计数
     */
    private long getMaxCount() {
        return (long) Math.pow(10, this.counterBits) - 1;
    }

    /**
     * 获取计数格式，默认长度2位，不足时前面用0补位
     *
     * @return 计数格式
     */
    private String getCountFormat() {
        return "%0" + this.counterBits + "d";
    }

    private class BizId {
        /**
         * 业务标识
         */
        private String bizMark;
        /**
         * 期数
         */
        private long period;

        private BizId(String bizMark) {
            this.bizMark = bizMark;
        }

        /**
         * 获取下一个业务ID
         *
         * @return 获取下一个业务ID
         * @throws BizIdException 当系统时钟回退时抛出或当计算值超过最大值时抛出，此两种情况下生成的主键可能重复
         */
        private String getNextId() {
            LocalDateTime currentDateTime;
            long count;

            synchronized (this) {
                currentDateTime = LocalDateTime.now();
                long currentPeriod = Long.valueOf(DateUtil.format(currentDateTime, resetLevel.CODE));

                // 当前期数小于原有期数，说明系统时钟可能被回退，生成的主键可能重复，抛出异常
                if (currentPeriod < this.period) {
                    throw new BizIdException(
                            String.format("Current period is %d, original period is %d, clock moved backwards. Refusing to generate count for %s", currentPeriod, this.period, this.bizMark)
                    );
                }

                //  当前期数大于原有期数，计数器重置
                if (currentPeriod > this.period) {
                    counter.reset(this.bizMark);
                    this.period = currentPeriod;
                }

                count = counter.increment(this.bizMark);
            }

            // 计数值超过最大计数，生成的主键可能重复，抛出异常
            if (count > maxCount) {
                throw new BizIdException(
                        String.format("Count can't be greater than %d. Refusing to generate count for %s", maxCount, this.bizMark)
                );
            }

            return DateUtil.format(currentDateTime, dateLevel.CODE) + machineCode + String.format(countFormat, count);
        }
    }

    public enum Level {
        YEAR("yyyy", "年"),
        MONTH("yyyyMM", "月"),
        DAY("yyyyMMdd", "日"),
        HOUR("yyyyMMddHH", "时"),
        MINUTE("yyyyMMddHHmm", "分"),
        SECOND("yyyyMMddHHmmss", "秒");

        /**
         * 编码
         */
        public final String CODE;
        /**
         * 描述
         */
        public final String DESC;

        Level(String code, String desc) {
            this.CODE = code;
            this.DESC = desc;
        }

    }
}
