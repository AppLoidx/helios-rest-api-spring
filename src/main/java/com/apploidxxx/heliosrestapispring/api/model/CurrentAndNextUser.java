package com.apploidxxx.heliosrestapispring.api.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Arthur Kupriyanov
 */
@Data@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CurrentAndNextUser {


    public CurrentAndNextUser(Set<com.apploidxxx.heliosrestapispring.entity.user.User> currentUsers){
        if (currentUsers != null)
        this.currentUsers = currentUsers.stream().map(User::new).collect(Collectors.toSet());
    }

    private Set<User> currentUsers;
    private User nextUser;

    @Data
    public static class User {

        public User(com.apploidxxx.heliosrestapispring.entity.user.User user){
            username = user.getUsername();
            fullname = user.getFirstName() + " " + user.getLastName();

            additionalInfo = new HashMap<>();
        }

        String username;
        String fullname;

        Map<String, String > additionalInfo;

    }

}
