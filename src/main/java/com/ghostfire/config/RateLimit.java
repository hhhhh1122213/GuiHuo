package com.ghostfire.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 接口限流注解，标注在 Controller 方法上 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流维度 key */
    String key();

    /** 时间窗口（秒） */
    int window() default 60;

    /** 窗口内最大请求数 */
    int maxCount() default 10;
}
