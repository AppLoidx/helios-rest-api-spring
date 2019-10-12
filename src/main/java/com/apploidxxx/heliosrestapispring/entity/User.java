package com.apploidxxx.heliosrestapispring.entity;


import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * Данные о пользователе
 *
 * @author Arthur Kupriyanov
 */
@Table(name="users")
@Entity
@EqualsAndHashCode(exclude = {"queueMember", "queueSuper"})
@Getter
@Setter
@NoArgsConstructor
public class User {

    public User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactDetails = new ContactDetails();
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


    @ManyToMany(mappedBy = "superUsers", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Queue> queueSuper;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private UserData userdata = new UserData(this);

    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Queue> queueMember;

    @JsonProperty("user_type")
    private UserType userType = UserType.STUDENT;

    @JoinColumn(name="session")
    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    private Session session;

    @JoinColumn(name = "contactDetails")
    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    private String groupName;

    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(name = "firstName", nullable = false)
    @JsonProperty("first_name")
    private String firstName;

    @Column(name = "lastName")
    @JsonProperty("last_name")
    private String lastName;

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


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
