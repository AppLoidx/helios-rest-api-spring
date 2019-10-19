package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * Чат очереди. Имеет отношение One-to-One c {@link Chat}
 *
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Chat {

    @JsonIgnore
    @Id
    @GeneratedValue
    private long id;

    public Chat(Queue queue){
        this.queue = queue;
    }

    @OneToOne(mappedBy = "chat")
    @JsonIgnore
    private Queue queue;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Message> messages;

    /**
     * Добавление нового сообщения в чат
     * @param user отправитель сообщения
     * @param message сообщение
     *
     * @see Message
     */
    public synchronized void newMessage(User user, String message){
        Message msg = new Message(user, message, this);
        if (messages==null){ messages = new LinkedHashSet<>();}

        messages.add(msg);
    }

}
