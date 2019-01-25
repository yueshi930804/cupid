package com.yue.cupid.spring.boot.autoconfigure.helper;

import com.yue.cupid.generator.MachineCodeGenerator;

/**
 * @author Yue
 * @since 2019/01/23
 * 机器码帮助者
 */
public class MachineCodeHelper {

    private static String machineCode;

    public MachineCodeHelper(MachineCodeGenerator machineCodeGenerator) {
        MachineCodeHelper.machineCode = machineCodeGenerator.getMachineCode();
    }

    /**
     * 获取机器码
     *
     * @return 机器码
     */
    public static String machineCode() {
        return MachineCodeHelper.machineCode;
    }
}
