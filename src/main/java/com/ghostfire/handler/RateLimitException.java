package com.ghostfire.handler;

/** 限流异常，由 GlobalExceptionHandler 捕获并返回 429 */
public class RateLimitException extends RuntimeException {

    public RateLimitException() {
        super("请求过于频繁，请稍后再试");
    }
}
