package com.limengyuan.partner.post.mapper;

import com.limengyuan.partner.common.entity.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分类数据访问层
 */
@Repository
public class CategoryMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Category> ROW_MAPPER = (rs, rowNum) -> Category.builder()
            .categoryId(rs.getInt("category_id"))
            .name(rs.getString("name"))
            .sortOrder(rs.getInt("sort_order"))
            .isActive(rs.getBoolean("is_active"))
            .build();

    public CategoryMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询所有启用的分类，按排序权重排序
     */
    public List<Category> findAllActive() {
        String sql = "SELECT category_id, name, sort_order, is_active FROM categories WHERE is_active = 1 ORDER BY sort_order ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    /**
     * 查询所有分类
     */
    public List<Category> findAll() {
        String sql = "SELECT category_id, name, sort_order, is_active FROM categories ORDER BY sort_order ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }
}
