package com.zengdaimoney.faunu.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresolver.DefaultTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * faunu配置
 *
 * @author wulj
 */
@Configuration
@ComponentScan(basePackages = {
    "com.zengdaimoney.faunu.internals"
})
@ConditionalOnProperty(name = {
        "faunu.project", "faunu.alarm.mail", "spring.mail.host",
        "spring.mail.username", "spring.mail.password", "spring.mail.port",
        "spring.mail.protocol"
})
public class TaskHealthConfig {



    /**
     * 如果没有SchedulerFactoryBean，则初始化一个
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({SchedulerFactoryBean.class})
    public SchedulerFactoryBean schedulerFactoryBean() {
        return new SchedulerFactoryBean();
    }

    @Bean
    @ConditionalOnMissingBean(SpringResourceTemplateResolver.class)
    public SpringResourceTemplateResolver templateResolver(){
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setCacheTTLMs(Long.valueOf(3600000L));
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine templateEngine(){
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;
    }


}
