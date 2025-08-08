package ru.poker.sportpoker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Slf4j
@Entity
@NoArgsConstructor
@Table(name = "avatars")
public class Avatar {

    public Avatar(UUID userId) {
        this.userId = userId;
    }

    @jakarta.persistence.Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(updatable = false)
    private UUID Id;

    private UUID userId;
}
