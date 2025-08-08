package ru.poker.sportpoker.service;

import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.dto.UploadFileResponse;
import ru.poker.sportpoker.dto.UserView;

import java.util.UUID;

public interface UserService {

    /**
     * Получение информации о пользователе
     *
     * @return
     */
    UserView getUser();

    /**
     * Загрузка аватара для пользователя
     *
     * @param userId Идентификатор пользователя
     * @param file Файл содержащий картинку
     * @return
     */
    UploadFileResponse uploadAvatar(UUID userId, MultipartFile file);

    /**
     * Получение аватара пользователя
     *
     * @param id Идентификатор пользователя
     * @return
     */
    MinioFileService.MinioFileResponse getAvatar(UUID id);
}
