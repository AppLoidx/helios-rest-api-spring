package com.apploidxxx.heliosrestapispring.entity.queue;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arthur Kupriyanov
 */
public class QueueTest {
    @Ignore
    @Test
    public void test_shuffle(){
        Queue q = new Queue("123");
        q.addUser(new User("111", "", "", ""));
        q.addUser(new User("222", "", "", ""));

        for (int i = 0; i < 10; i++){
            q.shuffle();
            System.out.println(q.getMembers());
        }
    }

    @Ignore
    @Test
    public void swap_test(){
        Queue q = new Queue("123");
        User u1 = new User("111", "", "", "");
        User u2 = new User("222", "", "", "");
        q.addUser(u1);
        q.addUser(u2);
        System.out.println(q.getMembers());
        q.swap(u1, u2);
        System.out.println(q.getMembers());
    }
}