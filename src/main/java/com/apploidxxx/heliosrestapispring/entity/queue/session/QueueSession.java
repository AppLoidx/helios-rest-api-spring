package com.apploidxxx.heliosrestapispring.entity.queue.session;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.session.statistic.Statistic;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Arthur Kupriyanov
 */
@Entity
@Data@NoArgsConstructor
@EqualsAndHashCode(exclude = "statistic")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class QueueSession {
    public QueueSession(Queue queue){
        this.queue = queue;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Queue queue;

    @OneToOne(cascade = CascadeType.ALL)
    private Statistic statistic = new Statistic(this);
}
