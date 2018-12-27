package me.marques.anderson.repository;

public class DocumentConflictException extends Exception {

    public DocumentConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}