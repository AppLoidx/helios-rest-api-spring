package com.apploidxxx.heliosrestapispring.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;


/**
 *
 * POJO объект для сообщений об ошибке
 *
 * @author Arthur Kupriyanov
 */
public class ErrorMessage {

    public ErrorMessage(String errorMessage, String errorDescription) {
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }

    @JsonAlias("error")
    public final String errorMessage;


    @JsonAlias("error_description")
    public final String errorDescription;

}
