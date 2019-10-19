package com.apploidxxx.heliosrestapispring.api.model;

import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Arthur Kupriyanov
 * @version 1.0.0
 */
@Data
public class UserSettings {

    @JsonIgnore
    private final User user;

    public UserSettings(User user){
        this.user = user;
        if (this.user == null){
            throw new NullPointerException();
        }
    }

    @JsonProperty("contact_details")
    public ContactDetails getContactDetails(){
        return this.user.getContactDetails();
    }

    @JsonProperty("username")
    public String getUsername(){
        return this.user.getUsername();
    }

}
