package com.zengdaimoney.faunu.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class ErrorVo implements Serializable {

    private static final long serialVersionUID = 8587159367447287857L;

    /**
     * 错误进程名
     */
    private String threadName;

    /**
     * 错误描述
     */
    private String description;

    /**
     * 发生时间
     */
    private Date createDate = new Date();
}
