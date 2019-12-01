package com.apploidxxx.heliosrestapispring.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.queue.controller.NextUserStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
public class QueueManager {

    /**
     * Moves user to the end of queue
     * <br><br>
     *
     * <span style="color: orange">Note</span> : this method don't modify the cursoredUsers field in {@link Queue}
     *
     * @param queue queue which contains this user
     * @param user user which needed to move
     * @return reordered Queue
     */
    public static Queue moveUserToEnd(Queue queue, User user)
    {
        LinkedList<User> linkedList = (LinkedList<User>) queue.getMembers();
        if (linkedList.isEmpty()) return queue;

        linkedList.remove(user);
        linkedList.addLast(user);
        queue.setMembers(linkedList);

        return queue;
    }
    public static Queue nextUser(Queue queue, User user){
        if (queue.getQueueSequence().isEmpty()) return queue;

        switch (queue.getQueueType()){
            case SINGLE: return singleQueueNext(queue, user);
            case MULTIPLE: return multipleQueueNext(queue, user);
            default:
                log.warn("Queue type " + queue.getQueueType() + " undefined");
                return queue;
        }
    }

    private static Queue singleQueueNext(Queue queue, User user){
        return NextUserStrategy.getDefaultStrategy().nextUser(user, queue);
    }

    private static Queue multipleQueueNext(Queue queue, User user){
        return NextUserStrategy.getMultiThreadedStrategy().nextUser(user, queue);
    }
}
