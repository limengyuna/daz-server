package com.limengyuan.partner.post.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.limengyuan.partner.common.entity.Category;
import com.limengyuan.partner.post.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务层
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /**
     * 获取所有启用的分类
     */
    public List<Category> getActiveCategories() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", 1).orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return categoryMapper.selectList(wrapper);
    }
}
