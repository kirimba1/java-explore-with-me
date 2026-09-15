package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e
            WHERE e.state = :state
              AND (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
              AND (:categories IS NULL OR e.category.id IN :categories)
              AND e.paid = COALESCE(:paid, e.paid)
              AND e.eventDate >= COALESCE(:rangeStart, e.eventDate)
              AND e.eventDate <= COALESCE(:rangeEnd, e.eventDate)
              AND (
                  COALESCE(:onlyAvailable, false) = false
                  OR COALESCE(e.confirmedRequests, 0) < e.participantLimit
              )
            """)
    List<Event> findPublicEvents(
            EventState state, String text, List<Long> categories,
            Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable
    );

    boolean existsByCategoryId(Long id);

    @Query("""
            SELECT e FROM Event e
            WHERE (:usersEmpty = true OR e.initiator.id IN :users)
              AND (:statesEmpty = true OR e.state IN :states)
              AND (:categoriesEmpty = true OR e.category.id IN :categories)
              AND e.eventDate >= COALESCE(:rangeStart, e.eventDate)
              AND e.eventDate <= COALESCE(:rangeEnd, e.eventDate)
            """)
    List<Event> findAdminEvents(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("usersEmpty") boolean usersEmpty,
            @Param("statesEmpty") boolean statesEmpty,
            @Param("categoriesEmpty") boolean categoriesEmpty
    );

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);
}