package com.apploidxxx.heliosrestapispring.entity.queue;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@Getter@Setter
public class CursoredUsersWrapper{
    @Id
    @GeneratedValue
    Long id;

    public CursoredUsersWrapper(Set<User> cursoredUsers){
        this.cursoredUsers = cursoredUsers;
    }

    @ManyToMany
    private Set<User> cursoredUsers;
}
