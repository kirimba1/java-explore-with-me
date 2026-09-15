package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Value
public class CompilationDto {
    @NotNull
    Long id;
    @NotNull
    Boolean pinned;
    @NotBlank
    String title;
    List<EventShortDto> events;
}