package com.apploidxxx.heliosrestapispring.api.filter.exception;

import com.apploidxxx.heliosrestapispring.api.exception.ResponsibleException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ResponsibleException.class)
    public ErrorMessage handleException(
            HttpServletResponse response,
            ResponsibleException e){
        return e.getResponse(response);
    }
}
