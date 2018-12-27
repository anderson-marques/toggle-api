package me.marques.anderson.security;

class AuthenticationFailedException extends Exception {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
