package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CategoryDto {
    Integer id;
    @Size(min = 1, max = 50)
    @NotBlank
    String name;
}


