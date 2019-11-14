package com.apploidxxx.heliosrestapispring.entity.access.repository.group;

import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface GroupRepository extends JpaRepository<UsersGroup, Long> {
    UsersGroup findByName(String name);
    void deleteByName(String name);
}
