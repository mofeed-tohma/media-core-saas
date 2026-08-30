package com.saas.media_core.exception;

import com.saas.media_core.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        log.warn("Bad Request Exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.systemDefault()),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Internal Server Error: ", ex);
        
        
        String realErrorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(ZoneId.systemDefault()),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                realErrorMessage
        );
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}