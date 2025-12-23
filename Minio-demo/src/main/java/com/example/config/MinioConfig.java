package com.example.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    /**
     * 硬编码配置（临时解决配置文件加载问题）
     */
    @Bean
    public MinioClient minioClient() {
        // 直接写死配置，避免读取application.yml
        String endpoint = "http://192.168.43.20:19966";
        String accessKey = "admin";
        String secretKey = "myj040520";

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}