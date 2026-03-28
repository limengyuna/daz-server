package com.limengyuan.partner.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 公共模块自动配置引导类
 *
 * 这个类不直接引用任何 Servlet/WebMvc 类，确保 Gateway（WebFlux）也能安全加载。
 * 仅在 Servlet 环境 + WebMvcConfigurer 存在时才扫描 common.config 和 common.interceptor 包。
 */
@Configuration
public class CommonAutoConfiguration {

    /**
     * Servlet 环境专用配置
     * 条件：1) 必须是 Servlet Web 应用  2) WebMvcConfigurer 类存在
     * 满足条件后通过 @ComponentScan 扫描 config 和 interceptor 包中的 @Component/@Configuration
     * excludeFilters 排除自身，防止循环导入
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    @ComponentScan(
            basePackages = {
                    "com.limengyuan.partner.common.config",
                    "com.limengyuan.partner.common.interceptor"
            },
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = CommonAutoConfiguration.class
            )
    )
    static class ServletWebConfig {
    }
}
