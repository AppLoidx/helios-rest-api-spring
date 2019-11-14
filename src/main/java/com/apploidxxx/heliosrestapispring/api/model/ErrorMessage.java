package com.apploidxxx.heliosrestapispring.api.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 *
 * POJO объект для сообщений об ошибке
 *
 * @author Arthur Kupriyanov
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorMessage {

    public ErrorMessage(String errorMessage, String errorDescription) {
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }

    public final String errorMessage;
    public final String errorDescription;

}
