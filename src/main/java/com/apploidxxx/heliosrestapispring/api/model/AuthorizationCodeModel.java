package com.apploidxxx.heliosrestapispring.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("authorization_code")
    private String authorizationCode;
}
