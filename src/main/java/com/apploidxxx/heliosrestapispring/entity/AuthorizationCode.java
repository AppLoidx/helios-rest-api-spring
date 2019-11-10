package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.persistence.*;
import java.util.Base64;
import java.util.Date;

/**
 *
 * Код авторизации при авторизации через Google OAuth 2.0
 *
 * С помощью {@link #authCode} можно получить access_token и refresh_token
 *
 * @author Arthur Kupriyanov
 */
@Entity
@Data
@NoArgsConstructor
public class AuthorizationCode {
    @Id
    @GeneratedValue
    private Long id;
    public AuthorizationCode(User user){
        this.user = user;
        authCode = new String(Base64.getEncoder().encode(Md5Crypt.md5Crypt((user.getUsername() + user.getFirstName() + new Date()).getBytes()).getBytes()));
    }

    @OneToOne
    private User user;

    @Column
    private String authCode;


}
