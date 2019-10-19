package com.apploidxxx.heliosrestapispring.api.model;


import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.SwapContainer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 *
 * POJO объект для информации о пользователе
 *
 * @see User
 * @author Arthur Kupriyanov
 */
@Data
public class UserInfo implements Serializable {
    private User user;

    private  List<Map<String, String>> queues;

    @JsonProperty("queues_member")
    private  List<Map<String, String>> queuesMember;

    @JsonProperty("swap_requests_in")
    private List<Map<String, String>> swapRequestsIn;

    @JsonProperty("swap_requests_out")
    private List<Map<String, String>> swapRequestsOut;

    public UserInfo(User user){
        this.user = user;
        this.swapRequestsIn = new ArrayList<>();
        this.swapRequestsOut = new ArrayList<>();
        initQueues(user);
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

    public List<Map<String, String>> getQueues() {
        return queues;
    }
}
