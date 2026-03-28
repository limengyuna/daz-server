package com.limengyuan.partner.common.interceptor;

import com.limengyuan.partner.common.util.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器：拦截微服务收到的 HTTP 请求
 * 提取网关塞进 Header 里的 X-Principal-Id 和 X-Principal-Type
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String principalIdStr = request.getHeader("X-Principal-Id");
        String principalType = request.getHeader("X-Principal-Type");

        if (StringUtils.hasText(principalIdStr)) {
            try {
                UserContextHolder.setPrincipalId(Long.parseLong(principalIdStr));
                UserContextHolder.setPrincipalType(principalType);
            } catch (NumberFormatException e) {
                // 如果格式错误，忽略即可。由后端各业务自主拦截是否必须登录
            }
        }
        
        // 恒等于 true，由各个 Controller 自主判断是否包含 userId，这个拦截器仅做装配不用来报错 401
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完毕必须清理，防止 Tomcat 线程池复用导致数据串门
        UserContextHolder.clear();
    }
}
