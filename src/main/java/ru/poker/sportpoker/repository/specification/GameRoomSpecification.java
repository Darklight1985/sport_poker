package ru.poker.sportpoker.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.enums.StatusGame;

import java.util.ArrayList;
import java.util.List;

@Builder
public class GameRoomSpecification implements Specification<GameRoom> {

    private String name;
    private StatusGame statusGame;

    @Override
    public Predicate toPredicate(Root<GameRoom> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
        }

        if (statusGame != null) {
            predicates.add(criteriaBuilder.equal(root.get("statusGame"), statusGame));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
    }
}
