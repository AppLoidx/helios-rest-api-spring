package com.apploidxxx.heliosrestapispring.queue.controller;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;

/**
 *
 * Use this strategy for use multithreaded queue strategy
 *
 * @author Arthur Kupriyanov
 */
public class MultiThreadedNextUserStrategy implements NextUserStrategy{
    MultiThreadedNextUserStrategy(){

    }

    @Override
    public Queue nextUser(User user, Queue queue) {
        return null;
    }
}
