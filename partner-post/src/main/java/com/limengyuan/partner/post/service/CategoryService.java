package com.limengyuan.partner.post.service;

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
        return categoryMapper.findAllActive();
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }
}
