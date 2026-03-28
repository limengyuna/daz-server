package com.limengyuan.partner.common.util;

/**
 * 用户上下文工具类，用于在微服务内部传递当前请求的登录用户身份
 */
public class UserContextHolder {
    
    // 存储当前线程的用户 ID，如果是管理员也是正数 (类型通过 Principal-Type 区分)
    private static final ThreadLocal<Long> PRINCIPAL_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PRINCIPAL_TYPE = new ThreadLocal<>();

    public static void setPrincipalId(Long id) {
        PRINCIPAL_ID.set(id);
    }

    public static Long getPrincipalId() {
        return PRINCIPAL_ID.get();
    }

    public static void setPrincipalType(String type) {
        PRINCIPAL_TYPE.set(type);
    }

    public static String getPrincipalType() {
        return PRINCIPAL_TYPE.get();
    }

    public static boolean isAdmin() {
        return "admin".equals(getPrincipalType());
    }

    public static void clear() {
        PRINCIPAL_ID.remove();
        PRINCIPAL_TYPE.remove();
    }
}
