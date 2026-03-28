package com.limengyuan.partner.gateway.filter;

import com.limengyuan.partner.common.util.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 全局统一鉴权过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 1. 完全公开，不需要 Token 的接口 (白名单)
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/**",
            "/api/admin/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**"
    );

    // 2. 可选登录，采用预编译正则表达式匹配 (防止宽泛路径误拦强制登录接口)
    private final List<Pattern> optionalAuthPatterns = Arrays.asList(
            Pattern.compile("^/api/activities$"),                  // 活动列表
            Pattern.compile("^/api/activities/\\d+$"),             // 活动详情
            Pattern.compile("^/api/activities/user/\\d+$"),        // 某用户的活动
            Pattern.compile("^/api/user/\\d+$"),                   // 用户公开资料
            Pattern.compile("^/api/user/\\d+/following.*$"),       // 用户关注列表
            Pattern.compile("^/api/user/\\d+/followers.*$"),       // 用户粉丝列表
            Pattern.compile("^/api/user/\\d+/follow-stats$"),      // 用户关注统计
            Pattern.compile("^/api/moments$"),                     // 动态列表
            Pattern.compile("^/api/moments/\\d+$"),                // 动态详情
            Pattern.compile("^/api/moments/user/\\d+$"),           // 某用户动态
            Pattern.compile("^/api/moments/\\d+/comments$"),       // 评论列表
            Pattern.compile("^/api/chat/messages/history/\\d+$")   // 历史消息记录
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();

        // 步骤 1：第一道防线，清洗由于客户端伪造的身份 Header
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .headers(headers -> {
                    headers.remove("X-Principal-Id");
                    headers.remove("X-Principal-Type");
                    headers.remove("X-User-Id"); // 兼容旧处理
                });

        // 步骤 2：判断是否为白名单接口（无需任何解析，直接放行）
        if (isMatch(path, publicPaths)) {
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        // 步骤 3：尝试获取 Token
        String token = getToken(request);
        boolean isOptional = isRegexMatch(path, optionalAuthPatterns);

        // 如果是必须登录的接口，且 Token 为空，则直接拒绝
        if (!StringUtils.hasText(token)) {
            if (isOptional) {
                // 可选登录且无 Token，放行
                return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
            } else {
                // 必须登录且无 Token，拦截
                return unauthorizedResponse(response, "未登录或 Token 缺失");
            }
        }

        // 步骤 4：解析并验证 Token
        Long userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            if (isOptional) {
                // 可选登录但 Token 伪造或失效，为防误导可以当作没登录放行，或者提示过期。这里选择抹除身份放行
                return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
            }
            return unauthorizedResponse(response, "Token 无效或已过期");
        }

        // 步骤 5：鉴别身份 (Admin 负数，User 正数) 并下游透传
        String principalType = userId < 0 ? "admin" : "user";
        Long principalId = Math.abs(userId);

        // 验证身份权限（防止纵向越权）
        boolean isAdminRoute = pathMatcher.match("/api/admin/**", path);
        if ("admin".equals(principalType) && !isAdminRoute) {
            // Admin Token 只允许访问 /api/admin/**，不可模拟普通用户去访问 C 端前台接口
            return forbiddenResponse(response, "当前为管理员身份，无权访问前台业务");
        }
        if ("user".equals(principalType) && isAdminRoute) {
            // 普通 User 不可访问后台管理接口
            return forbiddenResponse(response, "权限不足，无法访问后台接口");
        }

        requestBuilder.header("X-Principal-Type", principalType)
                      .header("X-Principal-Id", String.valueOf(principalId));

        // 步骤 6：检测并执行 Token 临期刷新 (将新 Token 放入响应头)
        if (JwtUtils.shouldRefreshToken(token)) {
            // 注意：此时用原始的 userId 生成，保持 Admin 的负数特征
            String newToken = JwtUtils.generateToken(userId);
            response.getHeaders().add("X-New-Token", newToken);
            // 暴露自定义 Header 给前端跨域访问
            response.getHeaders().add("Access-Control-Expose-Headers", "X-New-Token");
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private boolean isMatch(String path, List<String> patterns) {
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRegexMatch(String path, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    private String getToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader)) {
            return authHeader;
        }
        // 可选：从 Query 取
        return request.getQueryParams().getFirst("token");
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String json = String.format("{\"code\":401, \"message\":\"%s\", \"data\":null}", msg);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbiddenResponse(ServerHttpResponse response, String msg) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String json = String.format("{\"code\":403, \"message\":\"%s\", \"data\":null}", msg);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 较高优先级
    }
}
