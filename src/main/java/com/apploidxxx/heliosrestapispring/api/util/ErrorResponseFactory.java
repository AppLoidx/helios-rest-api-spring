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

    /**
     * Возвращает Response со статусом BAD_REQUEST (400)
     * @param description описание ошибки invalid_param
     * @return Response
     */
    public static ErrorMessage getInvalidParamErrorResponse(String description, HttpServletResponse response){
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ErrorMessage("invalid_param", description);

    }

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

}
