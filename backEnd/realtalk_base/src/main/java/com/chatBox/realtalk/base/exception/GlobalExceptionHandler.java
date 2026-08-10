package com.chatBox.realtalk.base.exception;

import com.chatBox.realtalk.base.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String traceId = getOrCreateTraceId(request);

        if (errorCode.getType() == ErrorType.SYSTEM_ERROR) {
            log.error("{} {} - [{}] {}", request.getMethod(), request.getRequestURI(), errorCode.getCode(), errorCode.getMessage(), ex);
        } else {
            log.warn("{} {} - [{}] {}", request.getMethod(), request.getRequestURI(), errorCode.getCode(), errorCode.getMessage());
        }

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(errorCode.getCode())
                        .type(errorCode.getType().name())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        String field = null;
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (field == null) {
                field = fieldError.getField();
            }
            details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-VAL-001] Request body validation failed. fields={}", request.getMethod(), request.getRequestURI(), details.keySet());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.INVALID_REQUEST.getCode())
                        .type(ErrorType.VALIDATION_ERROR.name())
                        .message("Dữ liệu không hợp lệ.")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .field(field)
                        .details(details)
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            details.putIfAbsent(path, violation.getMessage());
        });
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-VAL-002] Constraint violation. details={}", request.getMethod(), request.getRequestURI(), details);

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.INVALID_PARAMETER.getCode())
                        .type(ErrorType.VALIDATION_ERROR.name())
                        .message("Tham số yêu cầu không hợp lệ.")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .details(details)
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                        HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-VAL-001] Request body không đọc được.", request.getMethod(), request.getRequestURI());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.INVALID_REQUEST.getCode())
                        .type(ErrorType.VALIDATION_ERROR.name())
                        .message("Request body không hợp lệ hoặc không thể giải mã JSON.")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                                 HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-VAL-002] Thiếu tham số yêu cầu {}.", request.getMethod(), request.getRequestURI(), ex.getParameterName());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.INVALID_PARAMETER.getCode())
                        .type(ErrorType.VALIDATION_ERROR.name())
                        .message("Tham số yêu cầu bị thiếu: " + ex.getParameterName())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                            HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-VAL-002] Sai kiểu tham số {}.", request.getMethod(), request.getRequestURI(), ex.getName());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.INVALID_PARAMETER.getCode())
                        .type(ErrorType.VALIDATION_ERROR.name())
                        .message("Tham số yêu cầu sai kiểu: " + ex.getName())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                        HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.error("{} {} - [BASE-DB-001] Data integrity violation.", request.getMethod(), request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.DATA_INTEGRITY_VIOLATION.getCode())
                        .type(ErrorType.CONFLICT_ERROR.name())
                        .message(CommonErrorCode.DATA_INTEGRITY_VIOLATION.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                               HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-405-001] Phương thức không được hỗ trợ.", request.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.UNSUPPORTED_METHOD.getCode())
                        .type(ErrorType.SYSTEM_ERROR.name())
                        .message("Phương thức HTTP không được hỗ trợ.")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex,
                                                                 HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.warn("{} {} - [BASE-404-001] Endpoint không tồn tại.", request.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.PATH_NOT_FOUND.getCode())
                        .type(ErrorType.NOT_FOUND_ERROR.name())
                        .message("Không tìm thấy endpoint.")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(Exception ex,
                                                                     HttpServletRequest request) {
        String traceId = getOrCreateTraceId(request);
        log.error("{} {} - [BASE-SYS-001] Unexpected system exception.", request.getMethod(), request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.builder()
                        .success(false)
                        .code(CommonErrorCode.UNEXPECTED_ERROR.getCode())
                        .type(ErrorType.SYSTEM_ERROR.name())
                        .message(CommonErrorCode.UNEXPECTED_ERROR.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(traceId)
                        .build());
    }

    private String getOrCreateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
    }
}
