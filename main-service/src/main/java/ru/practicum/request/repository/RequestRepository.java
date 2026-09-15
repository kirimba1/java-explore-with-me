package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByUserId(Long userId);

    Optional<Request> findByEventIdAndUserId(Long eventId, Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> ids);
}
