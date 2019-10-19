package com.apploidxxx.heliosrestapispring.entity;


import com.apploidxxx.heliosrestapispring.entity.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.persistence.*;
import java.util.Base64;
import java.util.Date;

/**
 * Хранение информации о сессии с клиентом.
 *
 * Здесь хранятся access и refresh токены
 *
 * @author Arthur Kupriyanov
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Session {
    @Id
    @GeneratedValue
    Long id;

    @Column(name = "token",unique = true, nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "session")
    private User user;

    /**
     * Сгенерировать сессию
     *
     * Устанавливаем связь между {@link User} и {@link Session}
     *
     * Генерируем с помощью {@link Md5Crypt} токены для access и refresh
     * и кодируем в Base64
     *
     * @param user пользователь запросивший токены
     */
    public void generateSession(User user){
        user.setSession(this);
        this.user = user;
        accessToken = Base64.getEncoder().encodeToString(Md5Crypt.md5Crypt( (user.getUsername() + new Date().toString() + user.getFirstName()).getBytes() ).getBytes());
        refreshToken = Base64.getEncoder().encodeToString(Md5Crypt.md5Crypt( (user.getUsername() + new Date().toString() + user.getLastName()).getBytes() ).getBytes());
    }

}
