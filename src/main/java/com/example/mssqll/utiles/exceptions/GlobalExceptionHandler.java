package com.example.mssqll.utiles.exceptions;

import com.example.mssqll.service.WebhookNotifierService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final WebhookNotifierService webhookNotifierService;

    public GlobalExceptionHandler(WebhookNotifierService webhookNotifierService) {
        this.webhookNotifierService = webhookNotifierService;
    }

    private ResponseEntity<ErrorResponse> notifyAndBuildResponse(Exception ex, String userMessage, HttpStatus status) {
        webhookNotifierService.sendExceptionNotification(ex);
        ErrorResponse errorResponse = new ErrorResponse(userMessage, ex.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        return notifyAndBuildResponse(ex, "The requested endpoint does not exist.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException ex) {
        return notifyAndBuildResponse(ex, "Refresh token has some problems.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return notifyAndBuildResponse(ex, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex) {
        return notifyAndBuildResponse(ex, "Database error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenValidationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleTokenValidationException(TokenValidationException ex) {
        return notifyAndBuildResponse(ex, "Token validation failed.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FileAlreadyTransferredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleFileAlreadyTransferredException(FileAlreadyTransferredException ex) {
        return notifyAndBuildResponse(ex, "File transfer conflict.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return notifyAndBuildResponse(ex, "Access denied.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserIsDeletedException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ResponseEntity<ErrorResponse> handleUserIsDeletedException(UserIsDeletedException ex) {
        return notifyAndBuildResponse(ex, "User is deleted.", HttpStatus.GONE);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return notifyAndBuildResponse(ex, "User already exists.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DivideException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleDivideException(DivideException ex) {
        return notifyAndBuildResponse(ex, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AdminNotEditException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAdminEditException(AdminNotEditException ex) {
        return notifyAndBuildResponse(ex, "Admin edit not allowed.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FileNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleFileNotSupportedException(FileNotSupportedException ex) {
        return notifyAndBuildResponse(ex, "File format not supported.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Map<String, Object>> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex) {
        webhookNotifierService.sendExceptionNotification(ex);
        Map<String, Object> body = new HashMap<>();
        body.put("status", 499);
        body.put("error", "Client Disconnected");
        body.put("message", "The client connection was closed before the response could be sent.");
        return ResponseEntity.status(499).body(body);
    }

    // Generic handler for any other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return notifyAndBuildResponse(ex, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
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
