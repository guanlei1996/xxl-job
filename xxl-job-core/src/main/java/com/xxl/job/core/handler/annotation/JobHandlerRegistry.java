package com.xxl.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * annotation for job handler
 * @author 2016-5-17 21:06:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandlerRegistry {

    String cron() default "";
    String desc() default "";
    String author() default "";
    String email() default "";

    /**
     * 超时 默认 60 单位秒
     * @return
     */
    int timeout() default 0;
    int failRetryCount() default 0;
    
}
