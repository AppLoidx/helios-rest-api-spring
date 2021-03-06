package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface SessionRepository extends JpaRepository<Session, Long> {
    Session findByAccessToken(String accessToken);
}
