package com.apploidxxx.heliosrestapispring.api.model;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Arthur Kupriyanov
 */
@Data
@NoArgsConstructor
public class QueueShortInfo {

    private String queueName;
    private String queueFullname;
    private boolean isPrivate;
    private int membersCount;
    private Date creationDate;

    public QueueShortInfo(Queue q){
        this.queueName = q.getName();
        this.queueFullname = q.getFullname();
        this.isPrivate = q.getPassword()!=null;
        this.membersCount = q.getMembers().size();
        this.creationDate = q.getCreationDate();
    }
}
