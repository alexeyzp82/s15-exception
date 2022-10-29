package com.softserve.itacademy.exeption;

public class EntityNotFoundException extends RuntimeException {


    public EntityNotFoundException() {    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
