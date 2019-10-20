package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Дополнительные детали о пользователе, например, социальные сети и тд
 *
 * Также является POJO объектом для сериализации в информацию о пользователе
 *
 * @see com.apploidxxx.heliosrestapispring.api.model.UserInfo
 *
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ContactDetails {

    public ContactDetails(User user, String email, long vkontakteId){
        this.user = user;
        this.email = email;
        this.vkontakteId = vkontakteId;
    }
    public ContactDetails(User user, String email){
        this.user = user;
        this.email = email;
    }

    @Column
    private String img;

    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "contactDetails")
    private User user;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(unique = true)
    private Long vkontakteId;
}
