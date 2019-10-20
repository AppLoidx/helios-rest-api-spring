package com.apploidxxx.heliosrestapispring.api.util;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;

import javax.servlet.http.HttpServletResponse;

/**
 * Класс реализующий паттерн Фабрика.
 *
 * В качестве entity в Response принимается {@link ErrorMessage}
 *
 * @author Arthur Kupriyanov
 */
public abstract class ErrorResponseFactory {

    // *

    // BAD REQUEST STATUS - 400

    // *


    /**
     * Возвращает Response со статусом BAD_REQUEST (400)
     * @param title оглавление ошибки
     * @param description описание ошибки invalid_param
     * @return Response
     */
    public static ErrorMessage getInvalidParamErrorResponse(String title, String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ErrorMessage(title, description);
    }

    public static ErrorMessage getInvalidParamErrorResponse(String description, HttpServletResponse response){
        return getInvalidParamErrorResponse("invalid_param", description, response);
    }

    public static ErrorMessage getInvalidTokenErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory.getInvalidParamErrorResponse("invalid_token", "your token is invalid or expired", response);
    }

    // *

    // FORBIDDEN STATUS - 403

    // *

    /**
     * Возвращает Response со статусом FORBIDDEN (403)
     * @param description описание ошибки insufficient_rights
     * @return Response
     */
    public static ErrorMessage getForbiddenErrorResponse(String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return new ErrorMessage("insufficient_rights", description);
    }

    /**
     * Возвращает Response со статусом FORBIDDEN (403)
     * @return Response
     */
    public static ErrorMessage getForbiddenErrorResponse(HttpServletResponse response){
        return getForbiddenErrorResponse("You don't have enough rights", response);
    }


    // *

    // UNAUTHORIZED STATUS - 401

    // *

    public static ErrorMessage getUnauthorizedErrorResponse(String title, String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ErrorMessage(title, description);
    }


    // *

    // NOT FOUND STATUS - 404

    // *

    public static ErrorMessage getNotFoundErrorResponse(String title, String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ErrorMessage(title, description);
    }


    // *

    // NOT FOUND STATUS - 404

    // *

    public static ErrorMessage getInternalServerError(String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ErrorMessage("internal_server_error", description);
    }
}
