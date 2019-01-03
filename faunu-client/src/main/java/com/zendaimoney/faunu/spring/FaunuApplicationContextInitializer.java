package com.zendaimoney.faunu.spring;


import com.google.common.collect.Lists;
import com.zengdaimoney.faunu.internals.FaunuConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 初始化程序
 *
 * @author wulj
 */
@Slf4j
public class FaunuApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String alarmMail = environment.getProperty("faunu.alarm.mail");
        String env = environment.getProperty("env");
        String application = environment.getProperty("apollo.bootstrap.namespaces");

        if (StringUtils.isNotEmpty(alarmMail)) {
            FaunuConstants.ADMIN_MAIL = alarmMail;
            FaunuConstants.ENV = env;
            FaunuConstants.APPLICATION = application;
        }
    }

}
