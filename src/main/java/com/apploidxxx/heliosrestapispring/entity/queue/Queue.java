package com.apploidxxx.heliosrestapispring.entity.queue;


import com.apploidxxx.heliosrestapispring.entity.Chat;
import com.apploidxxx.heliosrestapispring.entity.queue.session.QueueSession;
import com.apploidxxx.heliosrestapispring.entity.queue.session.statistic.UserPassData;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Queue implements Serializable {

    public Queue(String name, String fullname){
        this.name = name;
        this.creationDate = new Date();
        this.chat = new Chat(this);
        this.fullname = fullname;
        this.notifications = new TreeSet<>();
        this.swapContainer = new SwapContainer(this);
    }

    public Queue(String name, String fullname, QueueType queueType){
        this(name, fullname);
        this.queueType = queueType;
    }

    public Queue(String name){
        this(name, name);
    }

    private QueueType queueType = QueueType.SINGLE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date creationDate;

    @Column
    private Boolean started = false;

    @Column
    private GenerationType generationType;

    @Column
    private String password;

    @Id
    @Column(name = "queue_name", unique = true)
    private String name;

    @Column(name = "fullname")
    private String fullname;

    @OrderColumn
    @ManyToMany
    @JoinTable(name="QUEUE_MEMBERS",
            joinColumns = {@JoinColumn(name="queue_name")},
            inverseJoinColumns={@JoinColumn(name="users_id")})
    private List<User> members;

    @ManyToMany
    @JoinTable(name="QUEUE_SUPER_USERS",
            joinColumns = {@JoinColumn(name="queue_name")},
            inverseJoinColumns={@JoinColumn(name="super_users")})
    private Set<User> superUsers;

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JsonIgnore
    private Chat chat;

    @Column
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private SwapContainer swapContainer;

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    private Map<User, CursoredUsersWrapper> cursoredUsers = new HashMap<>();

    @OneToOne(mappedBy = "queue", cascade = CascadeType.PERSIST)
    private QueueSession queueSession = new QueueSession(this);

    @OneToMany(mappedBy = "queue")
    private Set<UserPassData> userPassDataSet = new HashSet<>();

    public String getName() {
        return name;
    }


    public SwapContainer getSwapContainer(){
        if (swapContainer == null) swapContainer = new SwapContainer(this);

        return swapContainer;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void addSuperUser(User u){
        if (superUsers==null) superUsers= new HashSet<>();
        superUsers.add(u);
    }

    public void deleteSuperUser(User u){
        if (superUsers == null) return;
        superUsers.remove(u);
    }

    public void addUser(User u){
        if (members == null){
            members = new LinkedList<>();
        }
        members.add(u);

    }

    public void deleteUser(User u){
        if (members == null) return;
        members.remove(u);
    }

    public void shuffle(){
        Collections.shuffle(members);
    }

    public void swap(User user1, User user2 ) throws IndexOutOfBoundsException{
        int firstIndex = -1;
        int secondIndex = -1;
        int index = 0;
        for (User user : members){
            if (user1.equals(user)) firstIndex  = index;
            if (user2.equals(user)) secondIndex = index;

            index++;
        }

        Collections.swap(members, firstIndex, secondIndex);
    }

    public List<User> getMembersList() {
        if (members == null) members = new LinkedList<>();
        return members;
    }

    public Set<Notification> getNotifications(){
        if (this.notifications == null) this.notifications = new TreeSet<>();
        return this.notifications;
    }

    /**
     * Установка значения generation type через String
     *
     * Нужен даже с Setter аннотацией lombok
     *
     * @param type строковое значение GenerationType
     * @return <code>true</code> - если тип валидный, <code>false</code> если тип не опознан
     */
    public boolean setGenerationType(String type){
        GenerationType newGenerationType = GenerationType.getType(type);
        if (newGenerationType == GenerationType.NOT_STATED){
            return false;
        } else {
            this.generationType = newGenerationType;
            return true;
        }
    }
    @Transient
    @JsonProperty("queue_sequence")
    public List<Long> getQueueSequence(){
        List<Long> longList = new LinkedList<>();
        for (User u : members){
            longList.add(u.getId());
        }

        return longList;
    }

    public Map<User, CursoredUsersWrapper> getCursoredUsers(){
        if (cursoredUsers == null) {
            cursoredUsers = new HashMap<>();
        }
        return cursoredUsers;
    }

    public boolean isStarted(){
        if (started == null) started = false;

        return started;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Queue queue = (Queue) o;
        return Objects.equals(name, queue.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
