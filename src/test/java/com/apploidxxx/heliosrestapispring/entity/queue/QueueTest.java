package com.apploidxxx.heliosrestapispring.entity.queue;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

/**
 * @author Arthur Kupriyanov
 */
public class QueueTest {

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


    @Test
    public void swap_test(){
        Queue q = new Queue("123");
        User u1 = new User("111", "", "", "");
        User u2 = new User("222", "", "", "");
        u1.setId(1L);
        u2.setId(2L);
        q.addUser(u1);
        q.addUser(u2);
        q.swap(u1, u2);
    }
}