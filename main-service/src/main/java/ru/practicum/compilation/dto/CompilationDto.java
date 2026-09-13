package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.event.model.Event;

import java.util.List;

@Value
public class CompilationDto {
    @NotNull
    Integer id;
    @NotNull
    Boolean pinned;
    @NotBlank
    String title;
    List<Event> events;
}