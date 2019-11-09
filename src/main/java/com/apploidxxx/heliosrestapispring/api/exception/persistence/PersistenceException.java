package com.apploidxxx.heliosrestapispring.api.exception.persistence;

import com.apploidxxx.heliosrestapispring.api.exception.ResponsibleException;


/**
 * @author Arthur Kupriyanov
 */
public abstract class PersistenceException extends ResponsibleException {
    public PersistenceException(){
        this("Entity not found");
    }

    public PersistenceException(String message){
        super(message, new RuntimeException(), false, false);
    }
}
