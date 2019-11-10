package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, Long> {
    AuthorizationCode findByAuthCode(String authCode);
}
