package com.apploidxxx.heliosrestapispring.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;


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

    @JsonProperty("error")
    public final String errorMessage;


    @JsonProperty("error_description")
    public final String errorDescription;

}
