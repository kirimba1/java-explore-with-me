package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(
            EventsFilter eventsFilter, Integer from, Integer size, String ip
    );

    EventFullDto getEventsById(Long id, String ip);

    List<EventFullDto> getAdminEvents(
            AdminEventsFilter adminEventsFilter,
            Integer from, Integer size
    );

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequestDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addUserEvents(Long userId, NewEventDto newEventDto);

    EventFullDto getUserEventById(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto);
}
