package org.example.exceptions;

public class AuthenticationException extends BanKitException {
    public AuthenticationException() {
        super("Autentificare eșuată. ID sau parolă incorectă.");
    }
}
