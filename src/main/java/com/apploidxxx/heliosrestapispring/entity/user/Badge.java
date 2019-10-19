package com.apploidxxx.heliosrestapispring.entity.user;

import lombok.*;

import javax.persistence.*;

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
