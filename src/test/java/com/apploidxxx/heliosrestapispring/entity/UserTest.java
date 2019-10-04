package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class UserTest {


    @Test
    public void getQueueSuper() {
        User user = new User("", "", "", "", "");
        assertNotNull(user.getQueueSuper());
        Set<Queue> queueSet = new HashSet<>();
        Queue q1 = new Queue("111");
        Queue q2 = new Queue("222");
        queueSet.add(q1);
        queueSet.add(q2);
        user.setQueueSuper(queueSet);

        assertEquals(2, user.getQueueSuper().size());

    }

    @Test
    public void getQueueMember() {
        User user = new User("", "", "", "", "");
        assertNotNull(user.getQueueSuper());
        Set<Queue> queueSet = new HashSet<>();
        Queue q1 = new Queue("111");
        Queue q2 = new Queue("222");
        queueSet.add(q1);
        queueSet.add(q2);
        user.setQueueMember(queueSet);

        assertEquals(2, user.getQueueMember().size());
    }
}