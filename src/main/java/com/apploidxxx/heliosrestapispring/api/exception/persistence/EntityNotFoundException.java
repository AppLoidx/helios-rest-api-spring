package com.apploidxxx.heliosrestapispring.api.exception.persistence;

/**
 * @author Arthur Kupriyanov
 */
public class EntityNotFoundException extends PersistenceException {

    public EntityNotFoundException(){
        this("Entity not found");
    }

    public EntityNotFoundException(String message){
        super(message);
    }
}
