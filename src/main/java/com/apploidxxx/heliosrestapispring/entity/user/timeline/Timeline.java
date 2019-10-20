package com.apploidxxx.heliosrestapispring.entity.user.timeline;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data
public class Timeline implements Comparable<Timeline>{
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @Column
    private String text;
    @Column
    private Date date;
    @Column
    @JsonIgnore
    private TimelineTag category;
    @JsonIgnore
    @Column
    private String link;
    @JsonIgnore
    @Column
    private String url;

    @JsonIgnore
    public TimelineTag getCategory(){
        return category;
    }

    @JsonProperty("category")
    public Category getStringCategory(){
        return new Category(this.category.toString(), this.category.getColor());
    }
    @JsonProperty("link")
    public Link getLink(){
        return new Link(this.link, this.url);
    }

    @Override
    public int compareTo(Timeline o) {
        return (int) (o.getDate().getTime() - this.getDate().getTime());
    }
}
