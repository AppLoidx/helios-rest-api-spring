package com.apploidxxx.heliosrestapispring.entity.queue.session.statistic;

import com.apploidxxx.heliosrestapispring.entity.queue.session.QueueSession;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@NoArgsConstructor
@Entity
@Data
public class Statistic {

    public Statistic(QueueSession queueSession){

        this.queueSession = queueSession;
    }

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private QueueSession queueSession;

    @OneToMany
    private Set<UserPassData> userPassDataSet = new HashSet<>();

    public void addPassInfo(UserPassData userPassData){
        userPassDataSet.add(userPassData);
    }


}
