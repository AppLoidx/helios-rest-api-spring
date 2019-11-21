package com.apploidxxx.heliosrestapispring.api.util.chain;

import com.apploidxxx.heliosrestapispring.api.exception.ResponsibleException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
public class ActionNotFoundException extends ResponsibleException {
    @Override
    public ErrorMessage getResponse(HttpServletResponse response) {
        return ErrorResponseFactory.getInvalidParamErrorResponse("Property param not found", response);
    }
}
