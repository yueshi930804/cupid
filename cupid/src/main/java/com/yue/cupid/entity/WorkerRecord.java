package com.yue.cupid.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Yue
 * @since 2018/05/30
 * 工作者记录
 */
@Data
public class WorkerRecord {
    /**
     * ID
     */
    private Long id;
    /**
     * 工作者ID
     */
    private Integer workerId;
    /**
     * 数据中心ID
     */
    private Integer dataCenterId;
    /**
     * 工作者编码
     */
    private String workerCode;
    /**
     * 工作者名称
     */
    private String workerName;
    /**
     * 工作者主机
     */
    private String workerHost;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
