package com.ghostfire.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 公开接口 - 不需要登录
            SaRouter
                .match("/api/**")
                .notMatch(
                    "/api/auth/login",       // 登录
                    "/api/auth/register",     // 注册
                    "/api/posts/list",        // 帖子列表
                    "/api/posts/essence",      // 精华帖
                    "/api/posts/detail/**",    // 帖子详情
                    "/api/posts/search",      // 搜索帖子
                    "/api/category/list",     // 分类列表
                    "/api/comments/list",     // 评论列表
                    "/api/tags/list",         // 标签列表
                    "/api/users/*",           // 用户主页（公开）
                    "/api/users/*/posts",     // 用户帖子列表
                    "/api/boast/list",        // 吹牛列表
                    "/api/boast/*",           // 吹牛详情
                    "/api/medal/list",        // 勋章列表
                    "/api/ranking/coin",      // 金币排行
                    "/api/ranking/like",      // 获赞排行
                    "/api/ranking/post",       // 发帖排行
                    "/api/notifications/subscribe",  // SSE 订阅（token 从 query 参数传）
                    "/api/mini/auth/wx-login",     // 微信登录
                    "/api/mini/auth/logout",       // 登出
                    "/api/mini/posts/list",        // 帖子列表
                    "/api/mini/posts/*",           // 帖子详情
                    "/api/mini/comments/list",     // 评论列表
                    "/api/mini/user/*"             // 用户主页
                )
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}

