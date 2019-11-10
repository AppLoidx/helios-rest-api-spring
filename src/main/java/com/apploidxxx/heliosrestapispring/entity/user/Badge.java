package com.apploidxxx.heliosrestapispring.entity.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = {"user", "color"})
@NoArgsConstructor
public class Badge {

    public Badge(String text, String color, User user){
        this.text = text;
        this.color = color;
        this.user = user;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String text;
    private String color;

    @ManyToOne
    private User user;


}
