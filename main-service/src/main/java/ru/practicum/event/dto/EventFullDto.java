package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Value
public class EventFullDto {
    Integer id;
    @NotBlank
    String title;
    @NotBlank
    String annotation;
    String description;
    String state;
    @NotNull
    CategoryDto category;
    @NotNull
    Boolean paid;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    @NotNull
    UserShortDto initiator;
    Integer participantLimit;
    LocalDateTime createdOn;
    Boolean requestModeration;
    Integer confirmedRequests;
    LocalDateTime publishedOn;
    Integer views;

    @NotNull
    Location location;
    String moderationComment;
}