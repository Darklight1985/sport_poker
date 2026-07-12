package ru.poker.sportpoker.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioFileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.avatars}")
    private String bucketAvatar;

    @Value("${minio.bucket.cards}")
    private String bucketCards;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    @PostConstruct
    private void init() {
        createBucket(bucketAvatar);
        createCards(bucketCards);
    }

    public record MinioFileResponse(String contentType, InputStreamResource inputStreamResource) {
    }

    public void createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("Ошибка при создании bucket в MinIO {}", e.getMessage());
            throw new RuntimeException("Ошибка при создании bucket в MinIO", e);
        }
    }

    public void createCards(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath:cards/*");

                for (Resource resource : resources) {
                    System.out.println(resource.getFilename());
                    try {
                        InputStream inputStream = resource.getInputStream();
                        PutObjectArgs args = PutObjectArgs.builder()
                                .object(resource.getFilename())
                                .bucket(bucketCards)
                                .stream(inputStream, inputStream.available(), -1)
                                .build();
                        minioClient.putObject(args);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new RuntimeException("Ошибка сохранения файла");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при создании bucket в MinIO {}", e.getMessage());
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

    public MinioFileResponse download(String name) {
        GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketAvatar)
                    .object(name)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выгрузки файла: " + name);
        }

        String contentType = response.headers().get("content-type");
        return new MinioFileResponse(contentType, new InputStreamResource(response));
    }

    public MinioFileResponse downloadCard(String cardName, String suitName) {
        GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketCards)
                    .object(cardName + "_" + suitName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выгрузки карты: " + cardName);
        }

        String contentType = response.headers().get("content-type");
        return new MinioFileResponse(contentType, new InputStreamResource(response));
    }

    /**
     * Генерирует публичный URL для изображения карты.
     * Формат: {minioPublicUrl}/cards/{cardName}_{suitName}.jpg
     */
    public String getCardUrl(String cardName, String suitName) {
        return minioPublicUrl + "/cards/" + cardName + "_" + suitName + ".jpg";
    }

    /**
     * Генерирует URL для джокера.
     */
    public String getJokerUrl(String color) {
        return minioPublicUrl + "/cards/" + color.toLowerCase() + "_joker.jpg";
    }
}
