package com.zengdaimoney.chaos.exceptions;

/**
 * 批量任务
 *
 */
public class ScheduleStoppedException extends RuntimeException {

    public ScheduleStoppedException(String str) {
        super(str);
    }

    public ScheduleStoppedException(String str, Exception e) {
        super(str, e);
    }

}
