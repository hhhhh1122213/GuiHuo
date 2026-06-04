package com.ghostfire.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitInterceptorTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HandlerMethod handlerMethod;

    @InjectMocks RateLimitInterceptor interceptor;

    @Test
    void noAnnotation_passesThrough() throws Exception {
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(null);

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
        // 验证没有尝试限流（因为没有 @RateLimit 注解）
    }

    @Test
    void withAnnotation_passesThrough() throws Exception {
        RateLimit a = mock(RateLimit.class);
        when(a.key()).thenReturn("checkin:#{userId}");
        when(a.window()).thenReturn(86400);
        when(a.maxCount()).thenReturn(1);
        when(handlerMethod.getMethodAnnotation(RateLimit.class)).thenReturn(a);

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
        // @RateLimit 方法现在由 RateLimitAspect 处理，拦截器直接放行
    }
}