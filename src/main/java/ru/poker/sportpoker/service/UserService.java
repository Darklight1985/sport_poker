package ru.poker.sportpoker.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.dto.UploadFileResponse;
import ru.poker.sportpoker.dto.UserView;

import java.util.UUID;

public interface UserService {

    UserView getUser();

    UploadFileResponse uploadAvatar(UUID userId, MultipartFile file);

    ResponseEntity<InputStreamResource> getAvatar(UUID id);
}
