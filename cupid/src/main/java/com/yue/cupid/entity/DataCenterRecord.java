package com.yue.cupid.entity;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Yue
 * @since 2018/05/30
 * 数据中心记录
 */
@Data
public class DataCenterRecord {
    /**
     * ID
     */
    private Long id;
    /**
     * 数据中心ID
     */
    private Integer dataCenterId;
    /**
     * 数据中心编码
     */
    private String dataCenterCode;
    /**
     * 数据中心名称
     */
    private String dataCenterName;
    /**
     * 当前工作者数量
     */
    private Integer currentWorkerCount;
    /**
     * 累计启动计数
     */
    private Long totalStartCount;
    /**
     * 日启动计数
     */
    private Long dayStartCount;
    /**
     * 最后一次启动日期
     */
    private LocalDate lastStartDate;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
    /**
     * 更新时间
     */
    private LocalDateTime gmtModified;
}
