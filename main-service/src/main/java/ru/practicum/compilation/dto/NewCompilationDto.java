package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;

@Value
public class NewCompilationDto {
    Boolean pinned;
    @Size(min = 1, max = 50)
    @NotBlank
    String title;
    List<Integer> events;
}