package com.apploidxxx.heliosrestapispring.api.model;


import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author Arthur Kupriyanov
 */
@Getter@Setter
@NoArgsConstructor
public class Tokens {

    @JsonProperty("access_token")
    private String token;
    private String refreshToken;

    @JsonIgnore
    public User user;

    public Tokens(String token, String refreshToken, User user){
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

}
