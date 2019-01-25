package com.yue.cupid.test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Yue
 * @since 2017/10/20
 * 基础测试类，其他测试类继承即可
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
}
