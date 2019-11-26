package com.apploidxxx.heliosrestapispring.queue.controller;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;

import java.util.LinkedList;

/**
 *
 * Remove first user and put to the end of Queue
 *
 * @author Arthur Kupriyanov
 */
public class DefaultNextUserStrategy implements NextUserStrategy {

    DefaultNextUserStrategy(){

    }

    @Override
    public Queue nextUser(User user, Queue queue) {
        LinkedList<User> linkedList = new LinkedList<>(queue.getMembers());
        linkedList.addLast(linkedList.pollFirst());
        queue.setMembers(linkedList);
        return queue;
    }
}
