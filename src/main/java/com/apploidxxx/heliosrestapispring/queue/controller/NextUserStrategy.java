package com.apploidxxx.heliosrestapispring.queue.controller;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;

/**
 * @author Arthur Kupriyanov
 */
public interface NextUserStrategy {
    Queue nextUser(User user, Queue queue);

    static NextUserStrategy getDefaultStrategy(){
            return new DefaultNextUserStrategy();
    }

    static NextUserStrategy getMultiThreadedStrategy(){
        return new MultiThreadedNextUserStrategy();
    }
}
