package com.apploidxxx.heliosrestapispring.entity.user;


import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.commentary.Commentary;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.queue.CursoredUsersWrapper;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.session.statistic.UserPassData;
import com.apploidxxx.heliosrestapispring.entity.user.timeline.Timeline;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;


/**
 *
 * Данные о пользователе
 *
 * @author Arthur Kupriyanov
 */
@Table(name="users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class User {

    public User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactDetails = new ContactDetails();
        this.badges = new LinkedHashSet<>();
        this.timelines = new LinkedHashSet<>();

    }

    public User(String username, String password, String firstName, String lastName, String email){
        this(username, password, firstName, lastName);
        this.contactDetails.setEmail(email);
    }

    public User(String username, String password, String firstName, String lastName, String email, String group){
        this(username, password, firstName, lastName, email);
        this.groupName = group;
    }

    @Id
    @GeneratedValue
    Long id;


    @ManyToMany(mappedBy = "superUsers")
    @JsonIgnore
    private Set<Queue> queueSuper;


    @ManyToMany(mappedBy = "members", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Queue> queueMember;

    private UserType userType = UserType.STUDENT;

    @JoinColumn(name="session")
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    private Session session;

    @JoinColumn(name = "contactDetails")
    @OneToOne(cascade = CascadeType.ALL)
    private ContactDetails contactDetails;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    private String groupName;

    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @OneToMany
    private Set<Badge> badges;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Timeline> timelines;

    @OneToMany
    private Set<UserPassData> userPassDataSet = new LinkedHashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "users")
    private Set<UsersGroup> usersGroups = new LinkedHashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "groupSuperUsers")
    private Set<UsersGroup> usersGroupSuper = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "cursoredUsers")
    private Set<Queue> cursoredInQueues;

    @JsonIgnore
    @ManyToMany(mappedBy = "cursoredUsers")
    private Set<CursoredUsersWrapper> inCursoredUsersWrapper;

    @JsonIgnore
    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<Commentary> commentaries = new LinkedList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Commentary> commentariesAuthor = new LinkedList<>();

    public Set<UsersGroup> getUsersGroups(){
        if (usersGroups == null) usersGroups = new LinkedHashSet<>();
        return usersGroups;
    }

    public Set<UsersGroup> getUsersGroupSuper(){
        if (usersGroupSuper == null) usersGroupSuper = new HashSet<>();
        return usersGroupSuper;
    }

    public Set<UserPassData> getUserPassDataSet(){
        if (userPassDataSet == null) userPassDataSet = new LinkedHashSet<>();
        return userPassDataSet;
    }

    /**
     *
     * @return очереди которые этот пользователь администрирует
     */
    public Set<Queue> getQueueSuper() {
        if (queueSuper == null){
            queueSuper = new HashSet<>();
        }
        return queueSuper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id == null || user.id == null) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     *
     * @return очереди в которых этот пользователь является участником
     */
    public Set<Queue> getQueueMember() {
        if (queueMember ==null){
            queueMember = new HashSet<>();
        }
        return queueMember;
    }

    public Session getSession(){
        return this.session==null?new Session():this.session;
    }

    @Override
    public String toString() {
        return "User{" +
                " contactDetails=" + contactDetails +
                ", username='" + username + '\'' +
                ", groupName='" + groupName + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
