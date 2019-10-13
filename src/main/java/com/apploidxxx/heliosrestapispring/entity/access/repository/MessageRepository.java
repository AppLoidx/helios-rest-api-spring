package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

}
