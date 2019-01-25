package com.yue.cupid.spring.boot.autoconfigure;

import com.yue.cupid.generator.BizIdGenerator;
import com.yue.cupid.generator.MachineCodeGenerator;
import com.yue.cupid.generator.MachineCodeGenerator4DB;
import com.yue.cupid.spring.boot.autoconfigure.helper.BizIdHelper;
import com.yue.cupid.spring.boot.autoconfigure.helper.MachineCodeHelper;
import com.yue.cupid.spring.boot.autoconfigure.properties.CupidProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Yue
 * @since 2019/01/17
 */
@Configuration
@ConditionalOnClass(MachineCodeGenerator.class)
@ConditionalOnProperty(prefix = CupidProperties.CUPID_PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CupidProperties.class)
public class CupidAutoConfigure {

    @Resource
    private CupidProperties cupidProperties;

    @Bean(name = "machineCodeGenerator", initMethod = "generate", destroyMethod = "destroy")
    @ConditionalOnProperty(prefix = CupidProperties.DB_PREFIX, value = {"driver-class-name", "url", "username", "password"})
    @ConditionalOnMissingBean
    public MachineCodeGenerator machineCodeGenerator4DB() {
        return new MachineCodeGenerator4DB(
                this.cupidProperties.getDataCenterCode(), this.cupidProperties.getDataCenterName(), this.cupidProperties.getDataCenterBits(),
                this.cupidProperties.getWorkerBits(), this.cupidProperties.getDb()
        );
    }

    @Bean
    @ConditionalOnBean(MachineCodeGenerator.class)
    @ConditionalOnMissingBean
    public BizIdGenerator bizIdGenerator(@Autowired MachineCodeGenerator machineCodeGenerator) {
        return new BizIdGenerator(
                machineCodeGenerator.getMachineCode(), this.cupidProperties.getCounterBits(), this.cupidProperties.getResetLevel(),
                this.cupidProperties.getDateLevel()
        );
    }

    @Bean
    @ConditionalOnBean(BizIdGenerator.class)
    @ConditionalOnMissingBean
    public BizIdHelper bizIdHelper(@Autowired BizIdGenerator bizIdGenerator) {
        return new BizIdHelper(bizIdGenerator);
    }

    @Bean
    @ConditionalOnBean(MachineCodeGenerator.class)
    @ConditionalOnMissingBean
    public MachineCodeHelper machineCodeHelper(@Autowired MachineCodeGenerator machineCodeGenerator) {
        return new MachineCodeHelper(machineCodeGenerator);
    }
}
