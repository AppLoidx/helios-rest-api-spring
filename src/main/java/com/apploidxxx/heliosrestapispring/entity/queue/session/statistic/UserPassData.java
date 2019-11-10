package com.apploidxxx.heliosrestapispring.entity.queue.session.statistic;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data@NoArgsConstructor
public class UserPassData {

    public UserPassData(User user, Date startTime, Date endTime, Statistic statistic){

        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.statistic = statistic;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Statistic statistic;

    @ManyToOne
    private Queue queue;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
}
