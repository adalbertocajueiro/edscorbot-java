package es.us.edscorbot.jwt;

import jakarta.servlet.ServletException;

public class AuthenticationException extends ServletException{

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable rootCause) {
        super(rootCause);
    }

    public AuthenticationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
    
}   
