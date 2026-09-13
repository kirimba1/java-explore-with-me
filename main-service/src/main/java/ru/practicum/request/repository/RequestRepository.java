package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<ParticipationRequestDto> findAllByUserId(Long userId);

    Optional<ParticipationRequestDto> findByEventIdAndUserId(Long eventId, Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> ids);
}
