package com.apploidxxx.heliosrestapispring.api.exception;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * Super class of runtime exception, which
 * contains {@link #getResponse(HttpServletResponse)} method
 * to response to client {@link ErrorMessage} object with message and status
 *
 * @author Arthur Kupriyanov
 */
public abstract class ResponsibleException extends RuntimeException{
    public ResponsibleException(){
        this("responsible exception");
    }
    public ResponsibleException(String message){
        super(message, new RuntimeException(), false, false);
    }

    public ResponsibleException(String message, Throwable t, boolean enableSuppression, boolean writableStackTrace){
        super(message, t, enableSuppression, writableStackTrace);
    }

    public abstract ErrorMessage getResponse(HttpServletResponse response);
}
