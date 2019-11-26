package com.apploidxxx.heliosrestapispring.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.queue.controller.NextUserStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
public class QueueManager {
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
