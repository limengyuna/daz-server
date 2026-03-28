package com.limengyuan.partner.common.config;

import com.limengyuan.partner.common.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 微服务全局 MVC 配置，注册用户上下文拦截器
 * 注意：必须加上 Servlet 条件判断，因为 Gateway 是基于 WebFlux 的！不用 WebMvcConfigurer
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有进入 微服务 的请求
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**");
    }
}
