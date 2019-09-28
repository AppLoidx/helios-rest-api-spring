package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface AuthorizationCodeRepository extends CrudRepository<AuthorizationCode, Long> {
    AuthorizationCode findByAuthCode(String authCode);
}
