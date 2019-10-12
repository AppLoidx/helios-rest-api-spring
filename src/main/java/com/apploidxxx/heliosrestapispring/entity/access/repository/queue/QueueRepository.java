package com.apploidxxx.heliosrestapispring.entity.access.repository.queue;

import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface QueueRepository extends JpaRepository<Queue, String> {
    Queue findByName(String name);
}
