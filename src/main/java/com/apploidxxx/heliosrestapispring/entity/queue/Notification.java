package com.apploidxxx.heliosrestapispring.entity.queue;


import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Notification implements Comparable<Notification>{
    @Id
    @GeneratedValue
    private Long id;

    public Notification(User author, String message){
        this.author = author;
        this.userId = author==null?"null":author.getUsername();
        this.message = message;
        this.creationDate = new Date();
    }

    private Date creationDate;

    @JsonIgnore
    @ManyToOne(targetEntity = Queue.class)
    private Queue queue;

    @JsonIgnore
    @ManyToOne(targetEntity = User.class)
    private User author;

    @JsonProperty("user")
    private String userId;

    @Column(nullable = false)
    private String message;

    @Override
    public int compareTo(Notification o) {
        return (int) (o.creationDate.getTime() - this.creationDate.getTime());
    }
}
