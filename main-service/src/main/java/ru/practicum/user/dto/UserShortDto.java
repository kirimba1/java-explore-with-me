package ru.practicum.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;


@Value
public class UserShortDto {
    Integer id;

    @NotBlank
    String name;
}