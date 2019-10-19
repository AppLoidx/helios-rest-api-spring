package com.apploidxxx.heliosrestapispring.entity.queue;



import com.apploidxxx.heliosrestapispring.entity.Chat;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author Arthur Kupriyanov
 */
@EqualsAndHashCode(doNotUseGetters = true)
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Queue implements Serializable {

    public Queue(String name, String fullname){
        this.name = name;
        this.creationDate = new Date();
        this.queueSequence = new LinkedList<>();
        this.chat = new Chat(this);
        this.fullname = fullname;
        this.notifications = new TreeSet<>();
        this.swapContainer = new SwapContainer(this);
    }
    public Queue(String name){
        this(name, name);
    }

    @JsonProperty("creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date creationDate;

    @Column
    @JsonProperty("generation_type")
    private GenerationType generationType;

    @Column
    private String password;

    @Id
    @Column(name = "queue_name", unique = true)
    private String name;

    @Column(name = "fullname")
    private String fullname;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="QUEUE_MEMBERS",
            joinColumns = {@JoinColumn(name="queue_name")},
            inverseJoinColumns={@JoinColumn(name="users_id")})

    private Set<User> members;

    @JsonProperty("super_users")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="QUEUE_SUPER_USERS",
            joinColumns = {@JoinColumn(name="queue_name")},
            inverseJoinColumns={@JoinColumn(name="super_users")})
    private Set<User> superUsers;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JsonIgnore
    private Chat chat;

    @JsonProperty("queue_sequence")
    @OrderColumn(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(joinColumns = @JoinColumn(name = "queue_name"))
    private List<Long> queueSequence;

    @Column
    private String description;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private Set<Notification> notifications;

    @OneToOne(fetch =  FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private SwapContainer swapContainer;

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
        if (members==null) return;
        superUsers.remove(u);
    }

    public void addUser(User u){
        if (members==null) members= new HashSet<>();
        members.add(u);
        if (!queueSequence.contains(u.getId()))
        queueSequence.add(u.getId());
    }

    public void deleteUser(User u){
        if (members==null) return;
        members.remove(u);
        queueSequence.remove(u.getId());
    }

    public void shuffle(){
        Collections.shuffle(this.queueSequence);
    }

    void swap(User user1, User user2 ) throws IndexOutOfBoundsException{
        int firstIndex = -1;
        int secondIndex = -1;
        int index = 0;
        for (Long i : queueSequence){
            if (user1.getId().equals(i)) firstIndex = index;
            if (user2.getId().equals(i)) secondIndex= index;

            index++;
        }

        Collections.swap(queueSequence, firstIndex, secondIndex);
    }

    public List<User> getMembersList() {
        Map<Long, User> mapping = new HashMap<>();
        for (User u : members){
            mapping.put(u.getId(), u);
        }
        List<User> users = new LinkedList<>();
        for (Long id : queueSequence){
            users.add(mapping.get(id));
        }
        return users;
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
}
