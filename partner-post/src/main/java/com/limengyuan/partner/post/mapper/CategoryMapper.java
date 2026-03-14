package com.limengyuan.partner.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类数据访问层 - MyBatis-Plus
 *
 * 内置方法：
 * - selectList(null)     → 查询所有分类
 * - selectList(wrapper)  → 条件查询（如只查启用的）
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 所有方法都可以用 BaseMapper + QueryWrapper 实现，无需自定义 SQL
}
