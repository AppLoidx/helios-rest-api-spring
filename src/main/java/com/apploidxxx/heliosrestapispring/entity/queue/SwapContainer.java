package com.apploidxxx.heliosrestapispring.entity.queue;

import com.apploidxxx.heliosrestapispring.entity.User;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arthur Kupriyanov
 */
@ToString(exclude = "queue")
@Entity
public class SwapContainer {

    public SwapContainer(){
        this.swapMap = new HashMap<>();
    }
    public SwapContainer(Queue queue){
        this();
        this.queue = queue;
    }

    @Id
    @GeneratedValue
    private Long id;


    @OneToOne
    private Queue queue;

    @ElementCollection(targetClass= User.class, fetch = FetchType.EAGER)
    @MapKeyColumn(name="userMap")
    private Map<User , User> swapMap;

    /**
     * Добавляет запрос обмена мест, если обмен произошел, то возврает true, иначе false
     * @param user отправитель запроса
     * @param target цель запроса
     * @return true/false
     */
    public boolean addSwapRequest(User user, User target){
        swapMap.put(user, target);
        if (user.equals(swapMap.get(target))){
            queue.swap(user, target);
            swapMap.remove(user);
            swapMap.remove(target);
            return  true;
        } else {
            return false;
        }
    }
    /**
     * Возвращает цель запроса смены мест
     * @param user заказчик (запрашивающий)
     * @return пользователь (цель)
     */
    public User hasRequest(User user){
        return swapMap.get(user);
    }

    /**
     * Возвращает список пользователей запросивших смену мест
     * @param user цель
     * @return список пользователей
     */
    public List<User> getUserRequests(User user){
        List<User> users = new ArrayList<>();
        for (User u : swapMap.keySet()){
            if (swapMap.get(u).equals(user)){
                users.add(u);
            }
        }

        return users;
    }

    public Long getId() {
        return id;
    }
}
