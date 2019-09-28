package com.apploidxxx.heliosrestapispring.entity.access.repository.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface QueueRepository extends CrudRepository<Queue, Long> {
    Queue findByName(String name);
}
