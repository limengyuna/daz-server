package com.limengyuan.partner.common.config;

import com.limengyuan.partner.common.interceptor.UserContextInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 微服务全局 MVC 配置，注册用户上下文拦截器和全局异常处理器
 * 注意：必须加上 Servlet 条件判断，因为 Gateway 是基于 WebFlux 的，不用 WebMvcConfigurer
 *
 * 通过 @Bean 方式创建 UserContextInterceptor 和 GlobalExceptionHandler，
 * 避免将它们放入 AutoConfiguration.imports（会导致 Gateway 类加载失败）
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有进入微服务的请求
        registry.addInterceptor(userContextInterceptor())
                .addPathPatterns("/**");
    }
}
