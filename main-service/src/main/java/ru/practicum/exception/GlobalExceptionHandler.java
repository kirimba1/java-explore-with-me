package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        log.warn("NotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage(), List.of()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException e) {
        log.warn("ConflictException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", e.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        String message = errors.isEmpty() ? "Validation failed" : errors.getFirst();
        log.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("ConstraintViolationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format(
                "Failed to convert value of type java.lang.String to required type %s; "
                        + "nested exception is java.lang.NumberFormatException: For input string: %s",
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown",
                e.getValue()
        );
        log.warn("Type mismatch: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message, List.of()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(), List.of()));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleUnexpected(Throwable e) {
        log.error("Unexpected error", e);
        List<String> stackTrace = List.of(
                java.util.Arrays.stream(e.getStackTrace())
                        .limit(10)
                        .map(StackTraceElement::toString)
                        .toArray(String[]::new)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred.", e.getMessage(), stackTrace));
    }

    private String formatFieldError(FieldError error) {
        return String.format(
                "Field: %s. Error: %s. Value: %s",
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
        );
    }

    private ApiError buildError(HttpStatus status, String reason, String message, List<String> errors) {
        return new ApiError(errors, message, reason, status, LocalDateTime.now());
    }
}