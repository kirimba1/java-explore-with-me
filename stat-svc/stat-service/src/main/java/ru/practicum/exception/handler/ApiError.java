package ru.practicum.exception.handler;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    HttpStatus status;
    String error;
    String message;
}
