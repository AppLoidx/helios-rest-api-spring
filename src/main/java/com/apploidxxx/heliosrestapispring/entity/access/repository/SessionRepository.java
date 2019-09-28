package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.Session;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface SessionRepository extends CrudRepository<Session, Long> {
    Session findByAccessToken(String accessToken);
}
