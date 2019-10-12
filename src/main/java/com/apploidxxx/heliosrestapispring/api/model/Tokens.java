package com.apploidxxx.heliosrestapispring.api.model;


import com.apploidxxx.heliosrestapispring.entity.User;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Arthur Kupriyanov
 */
public class Tokens {

    @JsonProperty("access_token")
    public final String token;

    @JsonProperty("refresh_token")
    public final String refreshToken;

    @JsonIgnore
    public final User user;

    public Tokens(String token, String refreshToken, User user){
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
