package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.ViewStatsRequest;
import ru.practicum.service.StatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody @Valid EndpointHitDto hitDto) {
        statsService.saveHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@Valid ViewStatsRequest request) {
        return statsService.getStats(
                request.getStart(), request.getEnd(),
                request.getUris(), request.isUnique()
        );
    }
}
