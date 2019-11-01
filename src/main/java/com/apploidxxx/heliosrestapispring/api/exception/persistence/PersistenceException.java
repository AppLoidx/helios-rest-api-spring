package com.apploidxxx.heliosrestapispring.api.exception.persistence;

/**
 * @author Arthur Kupriyanov
 */
public class PersistenceException extends RuntimeException {
    public PersistenceException(){
        this("Entity not found");
    }

    public PersistenceException(String message){
        super(message, new RuntimeException(), false, false);
    }
}
