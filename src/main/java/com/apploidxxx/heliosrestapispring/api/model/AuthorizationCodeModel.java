package com.apploidxxx.heliosrestapispring.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Arthur Kupriyanov
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationCodeModel {
    private String authorizationCode;
}
