package com.yue.cupid.test;

import com.yue.cupid.spring.boot.autoconfigure.helper.BizIdHelper;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Yue
 * @since 2019/01/24
 */
public class BizIdTest extends BaseTest {

    @Test
    public void id() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1000);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    BizIdHelper.id();
                }
                cdl.countDown();
            }).start();
        }
        cdl.await();
        long timeConsuming = System.currentTimeMillis() - start;
        log.info("耗时：{}ms，平均：{}个/ms", timeConsuming, (1000 * 10000) / timeConsuming);
    }
}
