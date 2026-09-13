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
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(
                from / size,
                size
        );

        Page<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable);
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation with id={}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() ->
                        new NotFoundException("Compilation with id=" + compId + " was not found"));

        return compilationMapper.toDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Delete compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Compilation with id=" + compId + " was not found"
                        )
                );

        compilationRepository.delete(compilation);

        log.info("Compilation deleted successfully: id={}", compId);
    }

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating compilation: title={}", newCompilationDto.getTitle());

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Creating compilation successfully: title={}", savedCompilation.getTitle());

        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Updating compilation: id={}, title={}", compId, updateCompilationRequest.getTitle());

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Compilation with id=" + compId + " was not found"
                        )
                );

        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Updating compilation successfully: title={}", savedCompilation.getTitle());

        return compilationMapper.toDto(savedCompilation);
    }
}
