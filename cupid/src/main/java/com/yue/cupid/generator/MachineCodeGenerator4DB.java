package com.yue.cupid.generator;

import com.yue.cupid.entity.DataCenterRecord;
import com.yue.cupid.exception.MachineCodeException;
import com.yue.cupid.util.MachineIpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Yue
 * @since 2019/01/15
 * 机器码生成器 - 数据库
 */
@Slf4j
public class MachineCodeGenerator4DB extends MachineCodeGenerator {

    private static final String SELECT_DATA_CENTER_RECORD = "select " +
            "`data_center_id`, `current_worker_count`, `total_start_count`, " +
            "`day_start_count`, `last_start_date` " +
            "from " +
            "`data_center_record` " +
            "where " +
            "`data_center_code` = ? " +
            "limit 1 " +
            "for update";
    private static final String SELECT_EXIST_DATA_CENTER_ID = "select " +
            "`data_center_id` " +
            "from " +
            "`data_center_record`";
    private static final String INSERT_DATA_CENTER_RECORD = "insert ignore into " +
            "`data_center_record` " +
            "(`data_center_id`, `data_center_code`, `data_center_name`, " +
            "`current_worker_count`, `total_start_count`, `day_start_count`) " +
            "values " +
            "(?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_DATA_CENTER_RECORD_4_GENERATE = "update " +
            "`data_center_record` " +
            "set " +
            "`current_worker_count` = `current_worker_count` + 1, `total_start_count` = `total_start_count` + 1, `day_start_count` = ?, " +
            "`last_start_date` = ? " +
            "where " +
            "`data_center_id` = ?";
    private static final String SELECT_EXIST_WORKER_ID = "select " +
            "`worker_id` " +
            "from " +
            "`worker_record` " +
            "where " +
            "`data_center_id` = ?";
    private static final String INSERT_WORKER_RECORD = "insert into " +
            "`worker_record` " +
            "(`worker_id`, `data_center_id`, `worker_code`, " +
            "`worker_name`, `worker_host`) " +
            "values " +
            "(?, ?, ?, ?, ?)";
    private static final String UPDATE_DATA_CENTER_RECORD_4_DESTROY = "update " +
            "`data_center_record` " +
            "set " +
            "`current_worker_count` = `current_worker_count` - 1 " +
            "where " +
            "`data_center_id` = ?";
    private static final String DELETE_WORKER_RECORD = "delete from " +
            "`worker_record` " +
            "where " +
            "`worker_id` = ? " +
            "and " +
            "`data_center_id` = ?";

    private JdbcTemplate jdbcTemplate;
    private PlatformTransactionManager transactionManager;

    public MachineCodeGenerator4DB(String dataCenterCode, String dataCenterName, Database db) {
        this.init(dataCenterCode, dataCenterName, db);
    }

    public MachineCodeGenerator4DB(String dataCenterCode, String dataCenterName, int dataCenterBits,
                                   int workerBits, Database db) {
        if (dataCenterBits < 1) {
            throw new MachineCodeException("Data center bits can't be less than 1");
        }
        if (workerBits < 1) {
            throw new MachineCodeException("Worker bits can't be less than 1");
        }

        this.dataCenterBits = dataCenterBits;
        this.workerBits = workerBits;
        this.init(dataCenterCode, dataCenterName, db);
    }

    /**
     * 生成机器码
     */
    public void generate() throws Exception {
        this.transactionTaskActuator(() -> {
            DataCenterRecord dataCenterRecord = this.getDataCenterRecord();

            int currentWorkerCount = dataCenterRecord.getCurrentWorkerCount();
            int maxWorkerId = this.getMaxWorkerId();
            if ((currentWorkerCount - 1) == maxWorkerId) {
                throw new MachineCodeException(String.format("Worker can't be greater than %d", maxWorkerId));
            }

            int dataCenterId = dataCenterRecord.getDataCenterId();
            long dayStartCount;

            LocalDate lastStartDate = dataCenterRecord.getLastStartDate();
            LocalDate currentDate = LocalDate.now();
            if (lastStartDate == null || lastStartDate.isBefore(currentDate)) {
                dayStartCount = 1L;
            } else {
                dayStartCount = dataCenterRecord.getDayStartCount() + 1L;
            }

            this.jdbcTemplate.update(UPDATE_DATA_CENTER_RECORD_4_GENERATE, dayStartCount, currentDate, dataCenterId);

            Set<Integer> existWorkerIds = new HashSet<>(this.jdbcTemplate.queryForList(SELECT_EXIST_WORKER_ID, Integer.class, dataCenterId));

            TreeSet<Integer> allWorkerIds = new TreeSet<>();

            for (int i = 0; i <= maxWorkerId; i++) {
                allWorkerIds.add(i);
            }

            allWorkerIds.removeAll(existWorkerIds);

            int workerId = allWorkerIds.pollFirst();
            String workerCode = this.dataCenterCode + ":" + workerId;
            String workerName = this.dataCenterName + ":" + workerId + "号工作者";
            String workerHost = MachineIpUtil.getHost();

            this.jdbcTemplate.update(INSERT_WORKER_RECORD, workerId, dataCenterId, workerCode, workerName, workerHost);

            this.generate(dataCenterId, workerId);
        });
    }

    /**
     * 销毁机器码
     */
    public void destroy() throws Exception {
        this.transactionTaskActuator(() -> {
            this.jdbcTemplate.update(UPDATE_DATA_CENTER_RECORD_4_DESTROY, this.dataCenterId);
            this.jdbcTemplate.update(DELETE_WORKER_RECORD, this.workerId, this.dataCenterId);
            super.destroy();
        });
    }


    /**
     * 获取数据中心记录
     *
     * @return 数据中心记录
     */
    private DataCenterRecord getDataCenterRecord() {
        TreeSet<Integer> allDataCenterIds = null;
        List<DataCenterRecord> dataCenterRecords = this.jdbcTemplate.query(SELECT_DATA_CENTER_RECORD, new BeanPropertyRowMapper<>(DataCenterRecord.class), this.dataCenterCode);
        while (dataCenterRecords.isEmpty()) {
            if (allDataCenterIds == null) {
                allDataCenterIds = new TreeSet<>();
            }
            if (allDataCenterIds.isEmpty()) {
                Set<Integer> existDataCenterIds = new HashSet<>(this.jdbcTemplate.queryForList(SELECT_EXIST_DATA_CENTER_ID, Integer.class));

                int currentDataCenterCount = existDataCenterIds.size();
                int maxDataCenterId = this.getMaxDataCenterId();
                if ((currentDataCenterCount - 1) == maxDataCenterId) {
                    throw new MachineCodeException(String.format("Data center can't be greater than %d", maxDataCenterId));
                }

                for (int i = 0; i <= maxDataCenterId; i++) {
                    allDataCenterIds.add(i);
                }

                allDataCenterIds.removeAll(existDataCenterIds);
            }

            int dataCenterId = allDataCenterIds.pollFirst();

            if (this.jdbcTemplate.update(INSERT_DATA_CENTER_RECORD, dataCenterId, this.dataCenterCode, this.dataCenterName, 0, 0L, 0L) == 1) {
                dataCenterRecords = this.jdbcTemplate.query(SELECT_DATA_CENTER_RECORD, new BeanPropertyRowMapper<>(DataCenterRecord.class), this.dataCenterCode);
            }
        }
        return dataCenterRecords.get(0);
    }

    /**
     * 初始化
     *
     * @param dataCenterCode 数据中心编码
     * @param dataCenterName 数据中心名称
     * @param db             数据库
     */
    private void init(String dataCenterCode, String dataCenterName, Database db) {
        this.dataCenterCode = dataCenterCode;
        this.dataCenterName = dataCenterName;
        this.init4Db(db);
    }

    /**
     * 初始数据库
     *
     * @param db 数据库
     */
    private void init4Db(Database db) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(db.getDriverClassName());
        dataSource.setUrl(db.getUrl());
        dataSource.setUsername(db.getUsername());
        dataSource.setPassword(db.getPassword());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    /**
     * 事务任务执行器
     *
     * @param task 任务
     */
    private void transactionTaskActuator(TransactionTask task) throws Exception {
        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            task.execute();
        } catch (Exception e) {
            this.transactionManager.rollback(status);
            throw e;
        }
        this.transactionManager.commit(status);
    }

    @Data
    public static class Database {
        /**
         * 驱动名称
         */
        private String driverClassName;
        /**
         * 数据库url
         */
        private String url;
        /**
         * 数据库用户名
         */
        private String username;
        /**
         * 数据库密码
         */
        private String password;
    }

    @FunctionalInterface
    public interface TransactionTask {

        /**
         * 执行
         */
        void execute() throws Exception;
    }
}
