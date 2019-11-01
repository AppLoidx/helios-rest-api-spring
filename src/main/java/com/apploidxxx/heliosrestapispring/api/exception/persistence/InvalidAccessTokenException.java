package com.apploidxxx.heliosrestapispring.api.exception.persistence;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
public class InvalidAccessTokenException extends PersistenceException{
    public InvalidAccessTokenException(){
        super("Invalid token exception");
    }
    public ErrorMessage getResponse(HttpServletResponse response){
        return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
    }
}
