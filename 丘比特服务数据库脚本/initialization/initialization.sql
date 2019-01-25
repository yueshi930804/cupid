SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for DATA_CENTER_RECORD
-- ----------------------------
DROP TABLE IF EXISTS `DATA_CENTER_RECORD`;
CREATE TABLE `DATA_CENTER_RECORD`
(
  `ID`                   bigint(20)                                              NOT NULL AUTO_INCREMENT COMMENT '序号（物理主键）',
  `DATA_CENTER_ID`       int(11)                                                 NOT NULL COMMENT '数据中心ID',
  `DATA_CENTER_CODE`     varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '数据中心编码',
  `DATA_CENTER_NAME`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '数据中心名称',
  `CURRENT_WORKER_COUNT` int(11)                                                 NOT NULL COMMENT '当前工作者数量',
  `TOTAL_START_COUNT`    bigint(20)                                              NOT NULL COMMENT '累计启动工作者次数',
  `DAY_START_COUNT`      bigint(20)                                              NOT NULL COMMENT '当日启动工作者次数',
  `LAST_START_DATE`      date                                                    NULL     DEFAULT NULL COMMENT '启动最后一个工作者的日期',
  `GMT_CREATE`           timestamp(0)                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `GMT_MODIFIED`         timestamp(0)                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE INDEX `UNIQ_DCI` (`DATA_CENTER_ID`) USING BTREE,
  UNIQUE INDEX `UNIQ_DCC` (`DATA_CENTER_CODE`) USING BTREE,
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '数据中心记录'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for WORKER_RECORD
-- ----------------------------
DROP TABLE IF EXISTS `WORKER_RECORD`;
CREATE TABLE `WORKER_RECORD`
(
  `ID`             bigint(20)                                              NOT NULL AUTO_INCREMENT COMMENT '序号（物理主键）',
  `WORKER_ID`      int(11)                                                 NOT NULL COMMENT '工作者ID',
  `DATA_CENTER_ID` int(11)                                                 NOT NULL COMMENT '数据中心ID',
  `WORKER_CODE`    varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '工作者编码',
  `WORKER_NAME`    varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '工作者名称',
  `WORKER_HOST`    varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '工作者主机',
  `GMT_CREATE`     datetime(0)                                             NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE INDEX `UNIQ_WI_DCI` (`WORKER_ID`, `DATA_CENTER_ID`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '工作者记录'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
