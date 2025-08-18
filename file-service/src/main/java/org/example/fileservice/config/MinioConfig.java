package org.example.fileservice.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String region;
    private Integer presignedUrlExpiry;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
    }
}
