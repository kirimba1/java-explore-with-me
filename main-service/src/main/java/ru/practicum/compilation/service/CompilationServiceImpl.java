package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventViewsService;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;
    private final EventViewsService eventViewsService;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation with id={}", compId);

        return toDto(findCompilation(compId));
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Delete compilation with id: {}", compId);

        compilationRepository.delete(findCompilation(compId));

        log.info("Compilation deleted successfully: id={}", compId);
    }

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating compilation: title={}", newCompilationDto.getTitle());

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);
        compilation.setEvents(resolveEvents(newCompilationDto.getEvents()));

        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Creating compilation successfully: title={}", savedCompilation.getTitle());

        return toDto(savedCompilation);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        log.info("Updating compilation: id={}, title={}", compId, dto.getTitle());

        Compilation compilation = findCompilation(compId);

        compilationMapper.updateCompilationFromDto(dto, compilation);

        if (dto.getEvents() != null) {
            compilation.setEvents(resolveEvents(dto.getEvents()));
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Updating compilation successfully: title={}", savedCompilation.getTitle());

        return toDto(savedCompilation);
    }

    private CompilationDto toDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                toShortDtos(compilation.getEvents())
        );
    }

    private List<EventShortDto> toShortDtos(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }

        List<String> uris = events.stream().map(e -> "/events/" + e.getId()).toList();
        Map<String, Long> views = eventViewsService.getViewsMap(uris, null, null);

        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        views.getOrDefault("/events/" + event.getId(), 0L).intValue(),
                        event.getConfirmedRequests()
                ))
                .toList();
    }

    private Compilation findCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found"));
    }

    private List<Event> resolveEvents(List<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return eventRepository.findAllById(
                eventIds.stream().map(Long::valueOf).toList()
        );
    }
}