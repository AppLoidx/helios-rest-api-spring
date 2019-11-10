package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface ContactDetailsRepository extends JpaRepository<ContactDetails, Long> {
    ContactDetails findByEmail(String email);
    ContactDetails findByVkontakteId(Long vkid);
}
