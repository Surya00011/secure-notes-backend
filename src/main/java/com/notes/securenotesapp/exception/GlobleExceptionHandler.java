package com.notes.securenotesapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobleExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFoundException(UserNotFoundException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String,String>> handleInvalidTokenException(InvalidTokenException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<Map<String,String>> handleEmailSendException(EmailSendException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(OAuthProviderConflictException.class)
    public ResponseEntity<Map<String,String>> handleOAuthProviderConflictException(OAuthProviderConflictException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String,String>> handleAuthenticationException(AuthenticationException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleNoteNotFoundException(NoteNotFoundException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EncryptionFailedException.class)
    public ResponseEntity<Map<String,String>> handleEncryptionFailedException(EncryptionFailedException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DecreptionFailedException.class)
    public ResponseEntity<Map<String,String>> handleDecreptionFailedException(DecreptionFailedException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeException(RuntimeException exception) {
        Map<String,String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleException(Exception exception){
        Map<String,String> error = new HashMap<>();
        error.put("message","Unexpected error occurred cannot process your request");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }    
}
