package com.apploidxxx.heliosrestapispring.api.model;

import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@Data
@NoArgsConstructor
public class GroupModel {
    private Set<Map<String, String>> users = new LinkedHashSet<>();
    private Set<Map<String, String>> superUsers = new LinkedHashSet<>();
    private UsersGroup group;

    public GroupModel(UsersGroup usersGroup){
        this.group = usersGroup;
        addUsers(usersGroup);
    }

    private void addUsers(UsersGroup usersGroup){
        for (User user : usersGroup.getUsers()){
            users.add(getUserMap(user));
        }

        for (User user : usersGroup.getGroupSuperUsers()){
            superUsers.add(getUserMap(user));
        }
    }

    private Map<String, String> getUserMap(User user){
        Map<String, String> userMap = new HashMap<>();
        userMap.put("username", user.getUsername());
        userMap.put("fullname", user.getFirstName() + " " + user.getLastName());
        userMap.put("img", user.getContactDetails().getImg());
        return userMap;
    }
}
