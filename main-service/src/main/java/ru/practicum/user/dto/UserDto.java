package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UserDto {
    Integer id;
    @NotBlank
    String name;
    @Email
    @NotBlank
    String email;
}