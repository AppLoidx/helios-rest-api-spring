package com.apploidxxx.heliosrestapispring.entity.group;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data
@NoArgsConstructor
public class UsersGroup {

    public UsersGroup(User creator, String name, String fullname, String description, String password){

        groupSuperUsers = new HashSet<>();
        users = new LinkedHashSet<>();

        groupSuperUsers.add(creator);
        this.name = name;
        this.password = password;
        this.fullname = fullname;
        this.description = description;
    }

    public UsersGroup(User creator, String name, String fullname, String description){
        this(creator, name, fullname, description, null);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String name;

    @JsonIgnore
    @Column
    private String password;


    @Column
    private String fullname;

    @Column
    private String description;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name="users_group",
            joinColumns = {@JoinColumn(name="group_name")},
            inverseJoinColumns={@JoinColumn(name="users")}
            )
    private Set<User> users;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name="super_users_group",
            joinColumns = {@JoinColumn(name="group_name")},
            inverseJoinColumns={@JoinColumn(name="super_users")}
    )
    private Set<User> groupSuperUsers;

    public void addSuperUser(User user){
        groupSuperUsers.add(user);
    }

    public void deleteSuperUser(User user){
        groupSuperUsers.remove(user);
    }


    public void addUser(User user){
        users.add(user);
    }

    public void deleteUser(){

    }


}
