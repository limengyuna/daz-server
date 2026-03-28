package com.limengyuan.partner.common.util;

/**
 * 分页参数校验工具类
 * 统一收口分页参数校验逻辑，消除各控制器中的重复代码
 */
public class PageHelper {

    /** 默认页码 */
    public static final int DEFAULT_PAGE = 0;
    /** 默认每页数量 */
    public static final int DEFAULT_SIZE = 10;
    /** 最大每页数量 */
    public static final int MAX_SIZE = 100;

    /**
     * 校验页码，保证不小于 0
     */
    public static int safePage(int page) {
        return Math.max(page, 0);
    }

    /**
     * 校验每页数量，不合法时使用默认值
     *
     * @param size        前端传入的 size
     * @param defaultSize 默认值
     * @param maxSize     上限
     */
    public static int safeSize(int size, int defaultSize, int maxSize) {
        return (size <= 0 || size > maxSize) ? defaultSize : size;
    }

    /**
     * 校验每页数量（使用默认上限 100）
     */
    public static int safeSize(int size) {
        return safeSize(size, DEFAULT_SIZE, MAX_SIZE);
    }

    /**
     * 根据 page 和 size 计算 SQL OFFSET
     */
    public static int offset(int page, int size) {
        return safePage(page) * safeSize(size);
    }
}
