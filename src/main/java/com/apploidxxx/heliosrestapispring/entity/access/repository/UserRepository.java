package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
