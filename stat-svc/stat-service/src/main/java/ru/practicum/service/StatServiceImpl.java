package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void saveHit(EndpointHitDto hitDto) {
        EndpointHit hit = statsMapper.toEntity(hitDto);
        statsRepository.save(hit);
        log.info("Save query {}", hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Get stats with parameters: start = {}, end = {}, uris = {}, unique = {}", start, end, uris, unique);

        if (end.isBefore(start)) {
            throw new ValidationException("End cannot be before start date");
        }

        List<ViewStatsDto> result = statsRepository.getStats(start, end, uris, unique);
        log.info("Result: {}", result);
        return result;
    }
}
