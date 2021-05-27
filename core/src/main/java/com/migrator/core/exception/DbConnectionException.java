package com.migrator.core.exception;

public class DbConnectionException extends RuntimeException{
    public DbConnectionException(String message) {
        super(message);
    }
}
