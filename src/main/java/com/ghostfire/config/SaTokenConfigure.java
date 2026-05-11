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
                    "/api/tags/list"          // 标签列表
                )
                .check(r -> StpUtil.checkLogin());

            // 管理员接口 - 需要管理员角色
            SaRouter.match("/api/admin/**", r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }
}

