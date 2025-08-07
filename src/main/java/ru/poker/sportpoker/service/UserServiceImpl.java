package ru.poker.sportpoker.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.domain.Avatar;
import ru.poker.sportpoker.dto.UploadFileResponse;
import ru.poker.sportpoker.dto.UserView;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.AvatarRepository;

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
    private final AvatarRepository avatarRepository;

    private static final long MAX_SIZE_AVATAR = 8388608L;
    private final Pattern pattern = Pattern.compile("([^\s]+(\\.(?i)(jpe?g|png|gif|bmp))$)");

    @Override
    public UserView getUser() {
        String userIdStr = keycloakUserService.getCurrentUser();
        UserRepresentation userRepresentation = keycloakUserService.getUserRepresentation(UUID.fromString(userIdStr));
        return userMapper.getUserView(userRepresentation);
    }

    @Override
    @Transactional
    public UploadFileResponse uploadAvatar(UUID userId, MultipartFile file) {
        if (file.getSize() > MAX_SIZE_AVATAR) {
            throw new RuntimeException("Максимальный размер фото не более 8 Мб");
        }
        Matcher matcher = pattern.matcher(Objects.requireNonNull(file.getOriginalFilename()));
        if (!matcher.find()) {
            throw new RuntimeException("Не подходящий формат аватара");
        }

        String fileName;
        String minioPathToFile;

        Avatar avatar = avatarRepository.findByUserId(userId).orElseGet(() ->
           avatarRepository.save(new Avatar(userId))
        );

        fileName = String.valueOf(avatar.getId());
        minioPathToFile = fileName;

        minioFileService.putObject(file, minioPathToFile, file.getContentType());
        return new UploadFileResponse(avatar.getId().toString(), file.getContentType(), file.getSize());
    }

    @Override
    public MinioFileService.MinioFileResponse getAvatar(UUID id) {
        Avatar avatar = avatarRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Аватарка для пользователя %s не найдена".formatted(id)));
        UUID avatarId = avatar.getId();
        return minioFileService.download(avatarId.toString());
    }
}
