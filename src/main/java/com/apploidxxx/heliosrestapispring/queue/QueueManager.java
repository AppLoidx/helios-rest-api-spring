package com.apploidxxx.heliosrestapispring.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;

import java.util.LinkedList;

/**
 * @author Arthur Kupriyanov
 */
public class QueueManager {
    public static Queue nextUser(Queue queue, User user){
        if (queue.getQueueSequence().isEmpty()) return queue;

        switch (queue.getQueueType()){
            case SINGLE: return singleQueueNext(queue);
            case DOUBLE: return doubleQueueNext(queue, user);
            case MULTIPLE: return multipleQueueNext(queue, user);
            default:
                return queue;
        }
    }

    private static Queue singleQueueNext(Queue queue){
        LinkedList<User> linkedList = new LinkedList<>(queue.getMembers());
        linkedList.addLast(linkedList.pollFirst());
        queue.setMembers(linkedList);
        return queue;
    }

    private static Queue doubleQueueNext(Queue queue, User user){
        return queue;
    }

    private static Queue multipleQueueNext(Queue queue, User user){

        LinkedList<Long> linkedList = (LinkedList<Long>) queue.getQueueSequence();
        linkedList.remove(user.getId());
        linkedList.addLast(user.getId());

        return queue;
    }
}
