package com.apploidxxx.heliosrestapispring.api.model;

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

    public final String errorMessage;
    public final String errorDescription;

}
