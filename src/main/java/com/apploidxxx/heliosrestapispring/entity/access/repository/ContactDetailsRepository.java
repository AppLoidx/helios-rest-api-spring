package com.apploidxxx.heliosrestapispring.entity.access.repository;

import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface ContactDetailsRepository extends CrudRepository<ContactDetails, Long> {
    ContactDetails findByEmail(String email);
    ContactDetails findByVkontakteId(Long vkid);
}
