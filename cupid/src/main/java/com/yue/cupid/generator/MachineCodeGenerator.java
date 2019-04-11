package com.yue.cupid.generator;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Yue
 * @since 2019/01/15
 * 机器码生成器
 */
@Slf4j
public abstract class MachineCodeGenerator {

    protected static final String LOG_PREFIX = "[MachineCodeGenerator]";

    /**
     * 数据中心编码
     */
    protected String dataCenterCode;
    /**
     * 数据中心名称
     */
    protected String dataCenterName;
    /**
     * 数据中心所占位数（默认2位）
     */
    protected int dataCenterBits = 2;
    /**
     * 工作者所在位数（默认2位）
     */
    protected int workerBits = 2;
    /**
     * 数据中心ID
     */
    protected Integer dataCenterId;
    /**
     * 工作者ID
     */
    protected Integer workerId;
    /**
     * 机器码
     */
    protected String machineCode;

    /**
     * 生成机器码
     */
    public abstract void generate() throws Exception;

    /**
     * 销毁机器码
     */
    public void destroy() throws Exception {
        log.info("{} Destroy machine code - {}", LOG_PREFIX, this.machineCode);
    }

    /**
     * 获取机器码
     *
     * @return 机器码
     */
    public String getMachineCode() {
        return this.machineCode;
    }

    /**
     * 生成机器码
     *
     * @param dataCenterId 数据中心ID
     * @param workerId     工作者ID
     */
    protected void generate(int dataCenterId, int workerId) {
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
        this.machineCode = String.format(this.getDataCenterIdFormat(), dataCenterId) + String.format(this.getWorkerIdFormat(), workerId);
        log.info("{} Generate machine code - {}", LOG_PREFIX, this.machineCode);
    }

    /**
     * 获取支持的最大数据中心ID，默认结果为99
     *
     * @return 最大数据中心ID
     */
    protected int getMaxDataCenterId() {
        return (int) Math.pow(10, this.dataCenterBits) - 1;
    }

    /**
     * 获取支持的最大工作者ID，默认结果为99
     *
     * @return 最大工作者ID
     */
    protected int getMaxWorkerId() {
        return (int) Math.pow(10, this.workerBits) - 1;
    }

    /**
     * 获取数据中心ID格式，默认长度2位，不足时前面用0补位
     *
     * @return 数据中心ID格式
     */
    protected String getDataCenterIdFormat() {
        return "%0" + this.dataCenterBits + "d";
    }

    /**
     * 获取工作者ID格式，默认长度2位，不足时前面用0补位
     *
     * @return 工作者ID格式
     */
    protected String getWorkerIdFormat() {
        return "%0" + this.workerBits + "d";
    }
}
