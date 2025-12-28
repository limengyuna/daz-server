package com.limengyuan.partner.file.controller;

import com.limengyuan.partner.common.result.Result;
import com.limengyuan.partner.file.service.OssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final OssService ossService;

    public FileController(OssService ossService) {
        this.ossService = ossService;
    }

    /**
     * 统一文件上传接口
     * POST /api/file/upload?type=activity
     * POST /api/file/upload?type=files
     * 
     * @param file 要上传的文件
     * @param type 文件类型/文件夹名称 (如: activity, files)
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "files") String type) {
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (!isImage(file)) {
            return Result.error("只支持上传图片文件");
        }
        
        try {
            String url = ossService.uploadFile(file, type);
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传接口
     * POST /api/file/upload/batch?type=activity
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "type", defaultValue = "files") String type) {
        
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!isImage(file)) {
                return Result.error("只支持上传图片文件");
            }
            try {
                String url = ossService.uploadFile(file, type);
                urls.add(url);
            } catch (IOException e) {
                return Result.error("文件上传失败: " + e.getMessage());
            }
        }
        return Result.success(urls);
    }

    /**
     * 头像上传（独立接口，可添加压缩、裁剪逻辑）
     * POST /api/file/upload/avatar
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        if (!isImage(file)) {
            return Result.error("只支持上传图片文件");
        }
        
        // TODO: 可在此添加头像特殊处理逻辑（压缩、裁剪为正方形等）
        
        try {
            String url = ossService.uploadFile(file, "avatar");
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}

