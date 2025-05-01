package com.notes.securenotesapp.exception;

public class OAuthProviderConflictException extends RuntimeException {
    public OAuthProviderConflictException(String message) {
        super(message);
    }
}
