package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class NewEventDto {
    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    @NotBlank
    String description;
    Boolean paid;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    Integer participantLimit;
    Boolean requestModeration;

    @NotNull
    LocationDto location;

    @NotNull
    Integer category;
}