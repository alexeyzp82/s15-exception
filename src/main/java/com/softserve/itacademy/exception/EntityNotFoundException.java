package com.softserve.itacademy.exception;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String message)
    {
        super(message);
    }
}
