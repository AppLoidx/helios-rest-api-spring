package com.apploidxxx.heliosrestapispring.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
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
     * <br>
     * <span style="color: orange">Note</span> : this method can add new user if you don't check that your provided user exist in queue
     *
     * @param queue queue which contains this user
     * @param user user which needed to move
     * @return reordered Queue
     */
    public static Queue moveUserToEnd(Queue queue, User user)
    {
        LinkedList<User> linkedList = new LinkedList<>(queue.getMembers());
        if (linkedList.isEmpty()) return queue;


        linkedList.remove(user);
        linkedList.addLast(user);
        queue.setMembers(linkedList);

        return queue;
    }

}
