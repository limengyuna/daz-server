package com.limengyuan.partner.post.controller;

import com.limengyuan.partner.common.entity.Category;
import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.post.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器 - 活动分类相关接口
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取所有启用的分类
     * GET /api/categories
     */
    @GetMapping
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return Result.success(categories);
    }
}
