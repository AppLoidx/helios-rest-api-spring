package com.apploidxxx.heliosrestapispring.api.model;


import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.SwapContainer;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * POJO объект для информации о пользователе
 *
 * @see User
 * @author Arthur Kupriyanov
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserInfo implements Serializable {
    private User user;

    private List<Map<String, String>> queues;
    private List<Map<String, String>> queuesMember;

    private List<Map<String, String>> swapRequestsIn;
    private List<Map<String, String>> swapRequestsOut;

    private List<Map<String, String>> groupsMember;

    private List<QueueShortInfo> favorites;

    public UserInfo(User user){
        this.user = user;
        this.swapRequestsIn = new ArrayList<>();
        this.swapRequestsOut = new ArrayList<>();
        initQueues(user);
        initGroups(user);

        this.favorites = user.getFavorites().stream().map(QueueShortInfo::new).collect(Collectors.toList());
    }

    private void initGroups(User user){
        this.groupsMember = new ArrayList<>();

        for (UsersGroup usersGroup : user.getUsersGroupSuper()){
            Map<String, String> map = new HashMap<>();
            map.put("group_name", usersGroup.getName());
            map.put("group_fullname", usersGroup.getFullname());
            this.groupsMember.add(map);
        }

        for (UsersGroup usersGroup : user.getUsersGroups()){
            Map<String, String> map = new HashMap<>();
            map.put("group_name", usersGroup.getName());
            map.put("group_fullname", usersGroup.getFullname());
            this.groupsMember.add(map);
        }
    }

    private void initQueues(User user){
        Set<Map<String, String>> allList = new HashSet<>();
        Set<Map<String, String>> memberList = new HashSet<>();
        Set<Queue> memberSet = new HashSet<>(user.getQueueMember());
        Set<Queue> superSet = new HashSet<>(user.getQueueSuper());
        for (Queue q: memberSet
        ) {
            Map<String, String > jsonObj = new HashMap<>();
            jsonObj.put("short_name", q.getName());
            jsonObj.put("fullname", q.getFullname());
            memberList.add(jsonObj);
            allList.add(jsonObj);
            // adding list of swap requests
            addSwapRequest(q, user);
        }
        superSet.removeAll(memberSet);
        for (Queue q: superSet
             ) {
            Map<String, String > jsonObj = new HashMap<>();
            jsonObj.put("short_name", q.getName());
            jsonObj.put("fullname", q.getFullname());
            allList.add(jsonObj);
        }
        this.queues = new ArrayList<>(allList);
        this.queuesMember = new ArrayList<>(memberList);
    }

    private void addSwapRequest(Queue queue, User user){
        SwapContainer sc = queue.getSwapContainer();
        List<User> requestedUsers = sc.getUserRequests(user);
        if (!requestedUsers.isEmpty()) {
            for (User userIter: requestedUsers
                 ) {
                swapRequestsIn.add(generateMap(queue, userIter));
            }
        }

        User target = sc.hasRequest(user);
        if (target != null) swapRequestsOut.add(generateMap(queue, target));

    }

    private Map<String, String> generateMap(Queue queue, User target){
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("queue_name", queue.getName());
        hashMap.put("queue_fullname", queue.getFullname());
        hashMap.put("username", target.getUsername());
        hashMap.put("firstname", target.getFirstName());
        hashMap.put("lastname", target.getLastName());
        hashMap.put("from", "" + queue.getQueueSequence().indexOf(user.getId()));
        hashMap.put("to", "" + queue.getQueueSequence().indexOf(target.getId()));
        return hashMap;
    }

    public User getUser() {
        return user;
    }

}
