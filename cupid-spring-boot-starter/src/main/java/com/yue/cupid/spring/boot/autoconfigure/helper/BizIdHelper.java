package com.yue.cupid.spring.boot.autoconfigure.helper;


import com.yue.cupid.generator.BizIdGenerator;

/**
 * @author Yue
 * @since 2019/01/23
 * 业务ID帮助者
 */
public class BizIdHelper {

    private static final String COMMON_BIZ_MARK = "0";

    private static BizIdGenerator bizIdGenerator;

    public BizIdHelper(BizIdGenerator bizIdGenerator) {
        BizIdHelper.bizIdGenerator = bizIdGenerator;
    }

    /**
     * 获取公共业务ID
     *
     * @return 公共业务ID
     */
    public static String id() {
        return BizIdHelper.bizIdGenerator.getNextId(COMMON_BIZ_MARK);
    }

    /**
     * 获取追加前缀的公共业务ID
     *
     * @param prefix 前缀
     * @return 追加前缀的公共业务ID
     */
    public static String id(String prefix) {
        return prefix + BizIdHelper.bizIdGenerator.getNextId(COMMON_BIZ_MARK);
    }

    /**
     * 获取下一个业务ID
     *
     * @param bizMark 业务标识
     * @return 下一个业务ID
     */
    public static String getNextId(String bizMark) {
        return BizIdHelper.bizIdGenerator.getNextId(bizMark);
    }

    /**
     * 获取追加前缀的下一个业务ID
     *
     * @param prefix  前缀
     * @param bizMark 业务标识
     * @return 追加前缀的下一个业务ID
     */
    public static String getNextId(String bizMark, String prefix) {
        return prefix + BizIdHelper.bizIdGenerator.getNextId(bizMark);
    }
}
