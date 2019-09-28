package com.apploidxxx.heliosrestapispring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Persistence-unit для хранения сообщений в чате ({@link Chat})
 *
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Message {

    public Message(User user, String message, Chat chat){
        this.user = user;
        this.message = message;
        this.chat = chat;

        this.username = this.user.getUsername();
        this.fullname=  this.user.getFirstName() + " " + this.user.getLastName();
    }

    private String username;
    private String fullname;

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Chat.class)
    private Chat chat;

    @Column
    private String message;

    @ManyToOne(targetEntity = User.class)
    @JsonIgnore
    private User user;


}
