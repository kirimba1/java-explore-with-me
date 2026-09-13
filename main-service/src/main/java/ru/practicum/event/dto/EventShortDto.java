package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Value
public class EventShortDto {
    Integer id;
    @NotBlank
    String title;
    @NotBlank
    String annotation;
    CategoryDto category;
    @NotNull
    Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    @NotNull
    UserShortDto initiator;
    Integer confirmedRequests;
    Integer views;
}