package com.example.mssqll.utiles.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "The requested endpoint does not exist.",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Refresh token has some problems.",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Database error occurred.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(TokenValidationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleTokenValidationException(TokenValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Token validation failed.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(FileAlreadyTransferredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleFileAlreadyTransferredException(FileAlreadyTransferredException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "File transfer conflict.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Access denied.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(UserIsDeletedException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ResponseEntity<ErrorResponse> handleUserIsDeletedException(UserIsDeletedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "User is deleted.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.GONE)
                .body(errorResponse);
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("User already exists.", ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handelException(Exception ex) {
        ex.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse(
                "Something went wrong",
                ex.getStackTrace().toString()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
    @ExceptionHandler(DivideException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handelException(DivideException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Something went wrong",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(AdminNotEditException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAdminEditException(AdminNotEditException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Admin edit not allowed.",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(FileNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleFileNotSupportedException(FileNotSupportedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "File format not supported.",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Setter
    @Getter
    public static class ErrorResponse {
        private String message;
        private String exception;

        public ErrorResponse(String message, String exception) {
            this.message = message;
            this.exception = exception;
        }
    }

}