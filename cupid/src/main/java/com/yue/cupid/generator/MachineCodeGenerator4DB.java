package com.yue.cupid.generator;

import com.yue.cupid.entity.DataCenterRecord;
import com.yue.cupid.exception.MachineCodeException;
import com.yue.cupid.util.MachineIpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
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

    private static final String SELECT_DATA_CENTER_RECORD = "SELECT " +
            "`DATA_CENTER_ID`, `CURRENT_WORKER_COUNT`, `TOTAL_START_COUNT`, " +
            "`DAY_START_COUNT`, `LAST_START_DATE` " +
            "FROM " +
            "`DATA_CENTER_RECORD` " +
            "WHERE " +
            "`DATA_CENTER_CODE` = ? " +
            "LIMIT 1 " +
            "FOR UPDATE";
    private static final String SELECT_EXIST_DATA_CENTER_ID = "SELECT " +
            "`DATA_CENTER_ID` " +
            "FROM " +
            "`DATA_CENTER_RECORD`";
    private static final String INSERT_DATA_CENTER_RECORD = "INSERT IGNORE INTO " +
            "`DATA_CENTER_RECORD` " +
            "(`DATA_CENTER_ID`, `DATA_CENTER_CODE`, `DATA_CENTER_NAME`, " +
            "`CURRENT_WORKER_COUNT`, `TOTAL_START_COUNT`, `DAY_START_COUNT`) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_DATA_CENTER_RECORD_4_GENERATE = "UPDATE " +
            "`DATA_CENTER_RECORD` " +
            "SET " +
            "`CURRENT_WORKER_COUNT` = `CURRENT_WORKER_COUNT` + 1, `TOTAL_START_COUNT` = `TOTAL_START_COUNT` + 1, `DAY_START_COUNT` = ?, " +
            "`LAST_START_DATE` = ? " +
            "WHERE " +
            "`DATA_CENTER_ID` = ?";
    private static final String INSERT_WORKER_RECORD = "INSERT INTO " +
            "`WORKER_RECORD` " +
            "(`WORKER_ID`, `DATA_CENTER_ID`, `WORKER_CODE`, " +
            "`WORKER_NAME`, `WORKER_HOST`) " +
            "VALUES " +
            "(?, ?, ?, ?, ?)";
    private static final String UPDATE_DATA_CENTER_RECORD_4_DESTROY = "UPDATE " +
            "`DATA_CENTER_RECORD` " +
            "SET " +
            "`CURRENT_WORKER_COUNT` = `CURRENT_WORKER_COUNT` - 1 " +
            "WHERE " +
            "`DATA_CENTER_ID` = ?";
    private static final String DELETE_WORKER_RECORD = "DELETE FROM " +
            "`WORKER_RECORD` " +
            "WHERE " +
            "`WORKER_ID` = ? " +
            "AND " +
            "`DATA_CENTER_ID` = ?";

    private JdbcTemplate jdbcTemplate;

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
            if (currentWorkerCount == maxWorkerId) {
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

            int workerId = (int) (dataCenterRecord.getTotalStartCount() % (maxWorkerId + 1));
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
                if (currentDataCenterCount == maxDataCenterId) {
                    throw new MachineCodeException(String.format("Data center can't be greater than %d", maxDataCenterId));
                }

                for (int i = 0; i <= maxDataCenterId; i++) {
                    allDataCenterIds.add(i);
                }

                allDataCenterIds.removeAll(existDataCenterIds);
            }

            int dataCenterId = allDataCenterIds.pollFirst();

            this.jdbcTemplate.update(INSERT_DATA_CENTER_RECORD, dataCenterId, this.dataCenterCode, this.dataCenterName, 0, 0L, 0L);
            dataCenterRecords = this.jdbcTemplate.query(SELECT_DATA_CENTER_RECORD, new BeanPropertyRowMapper<>(DataCenterRecord.class), this.dataCenterCode);
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
    }

    /**
     * 事务任务执行器
     *
     * @param task 任务
     * @throws SQLException SQL异常
     */
    private void transactionTaskActuator(TransactionTask task) throws Exception {
        Connection connection = this.jdbcTemplate.getDataSource().getConnection();
        try {
            connection.setAutoCommit(false);
            task.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
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
