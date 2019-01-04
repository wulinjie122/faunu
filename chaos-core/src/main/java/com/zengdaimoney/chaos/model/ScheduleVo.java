package com.zengdaimoney.chaos.model;

import lombok.Data;
import lombok.ToString;
import org.quartz.Trigger;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class ScheduleVo implements Serializable {
    private static final long serialVersionUID = 6945148351482030031L;

    private String jobName;

    private String jobGroup;

    private Trigger.TriggerState status;

    private Date nextFireTime;

    private Date previousFireTime;

    /**
     * 是否已发送邮件提醒 0-未提醒,1-已提醒
     */
    private Integer notifyStatus;
}
