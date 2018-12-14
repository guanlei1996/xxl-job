package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * .
 *
 * @author Lay
 * @date 2018年12月14日
 */
public class HandlerRegistryParam implements Serializable {
    private static final long serialVersionUID = 6830359021007060981L;

    private String jobCron;		// 任务执行CRON表达式 【base on quartz】
    private String jobDesc;     // 任务描述

    private String author;		// 负责人
    private String alarmEmail;	// 报警邮件

    private String executor;                // 执行器名称
    private String executorRouteStrategy;	// 执行器路由策略
    private String executorHandler;		    // 执行器，任务Handler名称
//    private String executorParam;		    // 执行器，任务参数
    private String executorBlockStrategy;	// 阻塞处理策略
    private int executorTimeout;     		// 任务执行超时时间，单位秒
    private int executorFailRetryCount;		// 失败重试次数

    private String childHandler;            // 子任务 appName:handler[appName:handler,appName:handler.....]

    private String glueType;		// GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum


    public String getJobCron() {
        return jobCron;
    }

    public void setJobCron(String jobCron) {
        this.jobCron = jobCron;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlarmEmail() {
        return alarmEmail;
    }

    public void setAlarmEmail(String alarmEmail) {
        this.alarmEmail = alarmEmail;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getExecutorRouteStrategy() {
        return executorRouteStrategy;
    }

    public void setExecutorRouteStrategy(String executorRouteStrategy) {
        this.executorRouteStrategy = executorRouteStrategy;
    }

    public String getExecutorHandler() {
        return executorHandler;
    }

    public void setExecutorHandler(String executorHandler) {
        this.executorHandler = executorHandler;
    }

    public String getExecutorBlockStrategy() {
        return executorBlockStrategy;
    }

    public void setExecutorBlockStrategy(String executorBlockStrategy) {
        this.executorBlockStrategy = executorBlockStrategy;
    }

    public int getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(int executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public int getExecutorFailRetryCount() {
        return executorFailRetryCount;
    }

    public void setExecutorFailRetryCount(int executorFailRetryCount) {
        this.executorFailRetryCount = executorFailRetryCount;
    }

    public String getChildHandler() {
        return childHandler;
    }

    public void setChildHandler(String childHandler) {
        this.childHandler = childHandler;
    }

    public String getGlueType() {
        return glueType;
    }

    public void setGlueType(String glueType) {
        this.glueType = glueType;
    }

    @Override
    public String toString() {
        return "HandlerRegistryParam{" +
                "jobCron='" + jobCron + '\'' +
                ", jobDesc='" + jobDesc + '\'' +
                ", author='" + author + '\'' +
                ", alarmEmail='" + alarmEmail + '\'' +
                ", executor='" + executor + '\'' +
                ", executorRouteStrategy='" + executorRouteStrategy + '\'' +
                ", executorHandler='" + executorHandler + '\'' +
                ", executorBlockStrategy='" + executorBlockStrategy + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", executorFailRetryCount=" + executorFailRetryCount +
                ", childHandler='" + childHandler + '\'' +
                ", glueType='" + glueType + '\'' +
                '}';
    }
}
