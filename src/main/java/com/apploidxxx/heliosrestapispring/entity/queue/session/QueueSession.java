package com.apploidxxx.heliosrestapispring.entity.queue.session;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.session.statistic.Statistic;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data@NoArgsConstructor
public class QueueSession {
    public QueueSession(Queue queue){
        this.queue = queue;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Queue queue;

    @OneToOne
    private Statistic statistic = new Statistic(this);
}
