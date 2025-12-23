package com.example.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class MinioOperator {

    @Autowired
    private MinioClient minioClient;

    // 硬编码配置值
    private final String bucketName = "my-project";  // 替换为实际桶名
    private final String endpoint = "http://192.168.43.20:19966";

    /**
     * 上传文件到MinIO
     *
     * @param content          文件字节数组
     * @param originalFilename 原始文件名（用于获取文件后缀）
     * @return 文件在MinIO中的访问URL
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        // 1. 校验文件
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("文件名格式错误");
        }

        // 2. 生成文件存储路径（按日期分类 + 随机文件名避免重复）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + fileSuffix;
        String objectName = dateDir + "/" + fileName;  // 最终存储路径（如：2024/06/15/xxx.jpg）

        // 3. 上传文件到MinIO（适配新版SDK）
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)  // 桶名称
                        .object(objectName)  // 文件存储路径
                        .stream(new ByteArrayInputStream(content), content.length, -1)  // 文件流（-1表示自动识别大小）
                        .contentType(getContentType(fileSuffix)) // 优化：设置正确的文件类型
                        .build()
        );

        // 4. 生成访问URL（关键修复：直接拼接endpoint，避免调用SDK的getEndpoint()）
        // 处理endpoint末尾的斜杠，避免URL拼接错误
        String baseUrl = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        String accessUrl = baseUrl + bucketName + "/" + objectName;

        return accessUrl;
    }

    /**
     * 优化：根据文件后缀设置ContentType，避免浏览器下载而非预览
     */
    private String getContentType(String suffix) {
        String contentType;
        switch (suffix.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
            case ".png":
            case ".gif":
                contentType = "image/" + suffix.substring(1);
                break;
            case ".pdf":
                contentType = "application/pdf";
                break;
            case ".txt":
                contentType = "text/plain";
                break;
            case ".mp4":
                contentType = "video/mp4";
                break;
            default:
                contentType = "application/octet-stream";
        }
        return contentType;
    }
}