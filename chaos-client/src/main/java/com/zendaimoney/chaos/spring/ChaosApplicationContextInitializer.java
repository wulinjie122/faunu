package com.zendaimoney.chaos.spring;


import com.zengdaimoney.chaos.internals.ChaosConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 初始化程序
 *
 * @author wulj
 */
@Slf4j
public class ChaosApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String alarmMail = environment.getProperty("chaos.alarm.mail");
        String env = environment.getProperty("env");
        String application = environment.getProperty("apollo.bootstrap.namespaces");

        if (StringUtils.isNotEmpty(alarmMail)) {
            ChaosConstants.ADMIN_MAIL = alarmMail;
            ChaosConstants.ENV = env;
            ChaosConstants.APPLICATION = application;
        }
    }

}
