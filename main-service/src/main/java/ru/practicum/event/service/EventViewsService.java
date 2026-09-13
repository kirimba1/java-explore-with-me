package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.StatsClient;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventViewsService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime STATS_EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final StatsClient statsClient;

    public Map<String, Long> getViewsMap(List<String> uris, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (uris.isEmpty()) {
            return Map.of();
        }

        LocalDateTime start = rangeStart != null ? rangeStart : STATS_EPOCH;
        LocalDateTime end = rangeEnd != null ? rangeEnd : LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(
                start.format(DATE_TIME_FORMATTER),
                end.format(DATE_TIME_FORMATTER),
                uris
        );

        return stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
    }
}