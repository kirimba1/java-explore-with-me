package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, String sort, Integer from, Integer size, String ip
    );

    EventFullDto getEventsById(Long id, String ip);

    List<EventFullDto> getAdminEvents(
            List<Long> users, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Integer from, Integer size
    );

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addUserEvents(Long userId, NewEventDto newEventDto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto);
}
