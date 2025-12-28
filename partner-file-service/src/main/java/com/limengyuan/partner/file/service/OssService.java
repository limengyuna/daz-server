package com.limengyuan.partner.file.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.limengyuan.partner.file.config.OssProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传服务
 */
@Service
public class OssService {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    public OssService(OSS ossClient, OssProperties ossProperties) {
        this.ossClient = ossClient;
        this.ossProperties = ossProperties;
    }

    /**
     * 上传文件到 OSS
     *
     * @param file   要上传的文件
     * @param folder 文件夹名称 (如: avatar, activity)
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 1. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = generateFileName(folder, extension);

        // 2. 设置文件元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // 3. 上传到 OSS
        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), fileName, inputStream, metadata);
        }

        // 4. 返回文件访问 URL
        return getFileUrl(fileName);
    }

    /**
     * 生成文件名: folder/yyyy/MM/dd/uuid.ext
     */
    private String generateFileName(String folder, String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%s/%s%s", folder, datePath, uuid, extension);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取文件访问 URL
     */
    private String getFileUrl(String fileName) {
        return String.format("https://%s.%s/%s",
                ossProperties.getBucketName(),
                ossProperties.getEndpoint(),
                fileName);
    }
}
