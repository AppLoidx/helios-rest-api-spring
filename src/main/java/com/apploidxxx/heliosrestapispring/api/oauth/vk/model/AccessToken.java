package com.apploidxxx.heliosrestapispring.api.oauth.vk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Arthur Kupriyanov
 */
@Data
@NoArgsConstructor
public class AccessToken {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private String expiresIn;
    private String error;
    @JsonProperty("error_description")
    private String errorDescription;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("email")
    private String email;
}
