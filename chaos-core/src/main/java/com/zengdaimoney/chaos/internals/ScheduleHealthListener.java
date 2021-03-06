package com.zengdaimoney.chaos.internals;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zengdaimoney.chaos.model.ErrorVo;
import com.zengdaimoney.chaos.model.ScheduleVo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * quartz批量任务监听
 *
 * @author wulj
 */
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ScheduleHealthListener {

    /**
     * quartz工厂
     */
    @Autowired
    private Scheduler scheduler;

    /**
     * thymeleaf模板引擎
     */
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 线程工厂
     */
    private final static ThreadFactory factory;

    /**
     * 线程池
     */
    private final static ScheduledExecutorService m_executorService;


    /**
     * 刷新间隔时间单位
     */
    private static TimeUnit refreshIntervalTimeUnit = TimeUnit.MINUTES;

    /**
     * 刷新间隔时间 10分钟
     */
    private static int refreshInterval = 10;

    /**
     * 邮件模板名
     */
    private final static String TEMPLATE_NAME = "schedule_warning";

    /**
     * 批量任务列表
     */
    private static Map<String, ScheduleVo> taskList = new HashMap<String, ScheduleVo>();

    @Autowired
    private MailHandler mailHandler;

    static {
        // 初始化线程池
        factory = new ThreadFactoryBuilder().setNameFormat("sche-health-%d").build();
        m_executorService = Executors.newScheduledThreadPool(1, factory);
    }

    public ScheduleHealthListener() {
        // 定时监控job运行情况
        schedulePeriodicRefresh();
    }

    public void schedulePeriodicRefresh() {
        log.info("start monitor schedule health......");
        m_executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ErrorVo> errors = Lists.newArrayList();
                    for (String groupName : scheduler.getJobGroupNames()) {
                        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
                        Iterator<JobKey> iterator = jobKeys.iterator();
                        while (iterator.hasNext()) {
                            JobDetail jobDetail = scheduler.getJobDetail(iterator.next());
                            JobKey jobKey = jobDetail.getKey();
                            final List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                            Date nextFireTime = null;
                            Date previousFireTime = null;
                            int priority = 5;
                            Trigger.TriggerState triggerState = Trigger.TriggerState.PAUSED;

                            if (!CollectionUtils.isEmpty(triggers)) {
                                for (Trigger trigger : triggers) {
                                    nextFireTime = trigger.getNextFireTime();
                                    previousFireTime = trigger.getPreviousFireTime();
                                    priority = trigger.getPriority();
                                    triggerState = scheduler.getTriggerState(trigger.getKey());

                                    // 如果JOB不存在list中，则新增job
                                    if (!existsTask(jobKey.getGroup() + "." + jobKey.getName())) {
                                        addTask(jobKey);
                                    }

                                    // 如果阻塞了
                                    ScheduleVo scheduleVo = taskList.get(jobKey.getGroup() + "." + jobKey.getName());

                                    if ((Trigger.TriggerState.BLOCKED.equals(triggerState) ||
                                            Trigger.TriggerState.ERROR.equals(triggerState) ||
                                            Trigger.TriggerState.PAUSED.equals(triggerState)) && scheduleVo.getNotifyStatus() == 0) {

                                        // 邮件提醒内容
                                        ErrorVo errorVo = new ErrorVo();
                                        errorVo.setThreadName(jobKey.getGroup() + "." + jobKey.getName());
                                        errorVo.setDescription("批量任务运行异常，目前状态：" + triggerState.name());
                                        errors.add(errorVo);
                                    }

                                    // 更新job状态
                                    scheduleVo.setStatus(triggerState);
                                    updateTask(jobKey, scheduleVo);
                                }
                            }
                        }

                    }

                    // 如果有异常，则发送邮件通知管理员
                    if (!CollectionUtils.isEmpty(errors)) {
                        mailHandler.sendHtmlEmail(ChaosConstants.ADMIN_MAIL.split(","), "批量任务运行异常", createEmailContent(errors));
                    }

                } catch (Exception e) {
                    log.error("监控批量任务异常", e);
                }
            }
        }, refreshInterval, refreshInterval, refreshIntervalTimeUnit);

    }

    /**
     * 判断是否存在批量任务
     *
     * @param key
     * @return
     */
    public boolean existsTask(String key) {
        return taskList.get(key) != null;
    }

    /**
     * 添加批量任务
     * @param jobKey
     */
    public void addTask(JobKey jobKey) {
        try {
            ScheduleVo vo = new ScheduleVo();
            vo.setJobGroup(jobKey.getGroup());
            vo.setJobName(jobKey.getName());
            final List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (triggers.get(0).getNextFireTime() != null) {
                vo.setNextFireTime(triggers.get(0).getNextFireTime());
            }

            if (triggers.get(0).getPreviousFireTime() != null) {
                vo.setPreviousFireTime(triggers.get(0).getPreviousFireTime());
            }

            Trigger.TriggerState status = scheduler.getTriggerState(triggers.get(0).getKey());
            vo.setStatus(status);
            vo.setNotifyStatus(0);

            taskList.put(jobKey.getGroup() + "." + jobKey.getName(), vo);
        } catch (Exception e) {
            log.error("====", e);
        }
    }

    /**
     * 更新批量任务状态
     *
     * @param jobKey
     * @param updateVo
     */
    public void updateTask(JobKey jobKey,ScheduleVo updateVo) {
        ScheduleVo vo = taskList.get(jobKey.getGroup() + "." + jobKey.getName());
        BeanUtils.copyProperties(updateVo, vo);
    }

    /**
     * 发送邮件提醒内容
     *
     * @return
     */
    private String createEmailContent(List<ErrorVo> errors) {
        String content = "";
        try {
            Context ctx = new Context();
            ctx.setVariable("env", ChaosConstants.ENV);
            ctx.setVariable("application", ChaosConstants.APPLICATION);
            ctx.setVariable("errorList", errors);
            content = templateEngine.process(TEMPLATE_NAME, ctx);
        } catch (Exception e) {
            log.error("===========", e);
        }

        return content;
    }
}
