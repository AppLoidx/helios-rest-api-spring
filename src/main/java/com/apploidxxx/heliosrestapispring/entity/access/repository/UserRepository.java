package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
