package ru.poker.sportpoker.repository;

import org.springframework.data.repository.CrudRepository;
import ru.poker.sportpoker.domain.Avatar;

import java.util.Optional;
import java.util.UUID;

public interface AvatarRepository extends CrudRepository<Avatar, UUID> {

    Optional<Avatar> findByUserId(UUID userId);
}
