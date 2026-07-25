package clt.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {

        logInfo(ex, request);

        Map<String, String> errors = new HashMap<>();

        // Извлекаем из BindingResult все ошибки валидации и формируем Map,
        // где ключ — название поля, а значение — текст ошибки.
        // Это позволяет вернуть клиенту структурированный ответ с ошибками.
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(field, errorMessage);
        });

        return createBadRequest(request.getRequestURI(), errors);
    }

    // MethodArgumentTypeMismatchException - когда не получается преобразовать значение в нужный нам тип
    // HandlerMethodValidationException - когда нарушаются правила валидации на параметрах метода в контроллере
    // MissingServletRequestParameterException - когда не передали обязательный параметр запроса
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleMismatchExceptions(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {

        logInfo(ex, request);
        return createBadRequest(request.getRequestURI(), Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class) // глобальный обработчик
    public ResponseEntity<ApiError> handleGlobalException(Exception ex, HttpServletRequest request) {

        log.error("Возникло глобальное исключение: {}", formatRequestInfo(request), ex);

        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                Collections.singletonMap("Error", "something error"));
    }

    private ResponseEntity<ApiError> createResponseEntity(HttpStatus status, String path,
                                                          Map<String, String> errors) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .message(status.getReasonPhrase())
                .path(path)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ApiError> createBadRequest(String path, Map<String, String> errors) {
        return createResponseEntity(
                HttpStatus.BAD_REQUEST,
                path,
                errors
        );
    }

    private void logInfo(Throwable ex, HttpServletRequest request) {
        log.info("Исключение было обработано: {}, {}", ex.getClass().getSimpleName(), formatRequestInfo(request));
        log.debug("Детали исключения: {}, {}", formatRequestInfo(request), ex.getMessage());
    }

    private String formatRequestInfo(HttpServletRequest request) {
        return String.format("Метод %s, путь %s, параметры %s, remoteAddress %s",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr());
    }


}
