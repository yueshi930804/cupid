package com.yue.cupid.spring.boot.autoconfigure.properties;

import com.yue.cupid.generator.BizIdGenerator;
import com.yue.cupid.generator.MachineCodeGenerator4DB;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Yue
 * @since 2019/01/15
 */
@ConfigurationProperties(prefix = CupidProperties.CUPID_PREFIX)
@Data
public class CupidProperties {

    public static final String CUPID_PREFIX = "cupid";
    public static final String DB_PREFIX = CUPID_PREFIX + ".db";
    /**
     * Data center code
     */
    private String dataCenterCode;
    /**
     * Data center name
     */
    private String dataCenterName;
    /**
     * Data center bits(Default 2 bits)
     */
    private int dataCenterBits = 2;
    /**
     * Worker bits(Default 2 bits)
     */
    private int workerBits = 2;
    /**
     * Counter bits(Default 8 bits)
     */
    private int counterBits = 8;
    /**
     * Reset level(Default daily reset)
     */
    private BizIdGenerator.Level resetLevel = BizIdGenerator.Level.DAY;
    /**
     * Date level(Default output second)
     */
    private BizIdGenerator.Level dateLevel = BizIdGenerator.Level.SECOND;
    /**
     * Database properties
     */
    private MachineCodeGenerator4DB.Database db;
}
