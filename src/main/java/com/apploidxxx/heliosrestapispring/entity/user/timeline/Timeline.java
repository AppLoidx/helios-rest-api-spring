package com.apploidxxx.heliosrestapispring.entity.user.timeline;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Timeline implements Comparable<Timeline>{

    public Timeline(User user, String text, Date date, TimelineTag tag){

        this.user = user;
        this.text = text;
        this.date = date;
        this.tag = tag;
    }
    public Timeline(User user, String text, TimelineTag tag){

        this(user, text, new Date(), tag);
    }

    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne
    private User user;

    @Column
    private String text;
    @Column
    private Date date;
    @Column
    @JsonIgnore
    private TimelineTag tag;

    @JsonIgnore
    public TimelineTag getCategory(){
        return tag;
    }

    @JsonProperty("category")
    public Category getStringCategory(){
        return new Category(this.tag.toString(), this.tag.getColor());
    }


    @Override
    public int compareTo(Timeline o) {
        return (int) (o.getDate().getTime() - this.getDate().getTime());
    }

    public static Timeline getQueueTimeline(User user, String text){
        return new Timeline(user, text, new Date(), TimelineTag.QUEUE);
    }

    public static Timeline getSwapTimeline(User user, String text){
        return new Timeline(user, text, new Date(), TimelineTag.SWAP);
    }

    public static Timeline getCommentaryTimeline(User user, String text){
        return new Timeline(user , text, new Date(), TimelineTag.COMMENTARY);
    }
}
