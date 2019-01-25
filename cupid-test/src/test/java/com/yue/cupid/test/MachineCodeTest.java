package com.yue.cupid.test;

import com.yue.cupid.spring.boot.autoconfigure.helper.MachineCodeHelper;
import org.junit.Test;

/**
 * @author Yue
 * @since 2019/01/25
 */
public class MachineCodeTest extends BaseTest{

    @Test
    public void machineCode(){
        log.info("机器码：{}", MachineCodeHelper.machineCode());
    }
}
