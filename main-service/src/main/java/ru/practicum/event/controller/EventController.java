package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            String text, List<Long> categories, Boolean paid,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            Boolean onlyAvailable, String sort,
            @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        return eventService.getEvents(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request.getRemoteAddr()
        );
    }

    @GetMapping("/{id}")
    public EventFullDto getEventsById(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventsById(id, request.getRemoteAddr());
    }
}
