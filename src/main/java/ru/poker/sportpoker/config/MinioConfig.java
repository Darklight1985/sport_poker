package ru.poker.sportpoker.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String minioEndPoint;
    @Value("${minio.port}")
    private Integer minioPort;
    @Value("${minio.key.access}")
    private String minioAccessKey;
    @Value("${minio.key.secret}")
    private String minioSecretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioEndPoint, minioPort, false)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}
