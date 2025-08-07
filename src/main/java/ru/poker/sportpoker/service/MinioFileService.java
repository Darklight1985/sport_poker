package ru.poker.sportpoker.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioFileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.avatars}")
    private String bucketAvatar;

    @PostConstruct
    private void init() {
        createBucket(bucketAvatar);
    }

    public void createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании bucket в MinIO", e);
        }
    }

    public ObjectWriteResponse putObject(MultipartFile file, String absolutePathToFile, String contentType) {
        try {
            InputStream inputStream = file.getInputStream();
            PutObjectArgs args = PutObjectArgs.builder()
                    .object(absolutePathToFile)
                    .contentType(contentType)
                    .bucket(bucketAvatar)
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            return minioClient.putObject(args);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Ошибка сохранения файла");
        }
    }
}
