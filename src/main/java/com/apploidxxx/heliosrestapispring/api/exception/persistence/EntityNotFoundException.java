package com.apploidxxx.heliosrestapispring.api.exception.persistence;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

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

    @Override
    public ErrorMessage getResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ErrorMessage("persistence_exception", "entity not found");
    }
}
