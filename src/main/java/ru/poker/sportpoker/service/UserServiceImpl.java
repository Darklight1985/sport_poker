package ru.poker.sportpoker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.dto.UploadFileResponse;
import ru.poker.sportpoker.dto.UserView;
import ru.poker.sportpoker.mapper.UserMapper;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final KeycloakUserService keycloakUserService;
    private final MinioFileService minioFileService;
    private final UserMapper userMapper;

    private static final long MAX_SIZE_AVATAR = 8388608L;
    private final Pattern pattern = Pattern.compile("([^\s]+(\\.(?i)(jpe?g|png|gif|bmp))$)");

    @Value("${minio.bucket.avatars}")
    private String bucketAvatar;

    @Override
    public UserView getUser() {
        String userIdStr = keycloakUserService.getCurrentUser();
        UserRepresentation userRepresentation = keycloakUserService.getUserRepresentation(UUID.fromString(userIdStr));
        return userMapper.getUserView(userRepresentation);
    }

    @Override
    public UploadFileResponse uploadAvatar(UUID userId, MultipartFile file) {
        if (file.getSize() > MAX_SIZE_AVATAR) {
            throw new RuntimeException("Максимальный размер фото не более 8 Мб");
        }
        Matcher matcher = pattern.matcher(Objects.requireNonNull(file.getOriginalFilename()));
        if (!matcher.find()) {
            throw new RuntimeException("Не подходящий формат аватара");
        }

        String formatName = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName;
        String minioPathToFile;

        fileName = "/" + UUID.randomUUID();
        minioPathToFile = bucketAvatar + fileName;

       minioFileService.putObject(file, minioPathToFile, file.getContentType());

       return new UploadFileResponse(fileName, file.getContentType(), file.getSize());
    }

    @Override
    public ResponseEntity<InputStreamResource> getAvatar(UUID id) {
        return null;
    }
}
