package com.apploidxxx.heliosrestapispring.api.oauth.vk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Arthur Kupriyanov
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VkUser {

    private String id;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("screen_name")
    private String screenName;
    @JsonProperty("photo_100")
    private String photo100Url;
    @JsonProperty("access_token")
    private String accessToken;
}
