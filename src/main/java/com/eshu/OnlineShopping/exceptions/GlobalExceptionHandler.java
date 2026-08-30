package com.eshu.OnlineShopping.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException exec){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }

    /** Thrown by AuthenticatedAccountResolver when a token/session isn't valid for the action being taken. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException exec){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exec.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException exec){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exec.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateException exec){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exec.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handleInsufficientStock(InsufficientStockException exec){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exec.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exec){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exec.getMessage());
    }

    /**
     * Fires when an upload exceeds spring.servlet.multipart.max-file-size /
     * max-request-size (see application.properties) - i.e. before
     * ProductService's own per-file size check ever runs, since the
     * request is rejected at the servlet layer first.
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException exec){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Upload too large. Each image must be 5MB or smaller, and a single upload must total under 40MB.");
    }

}
