package com.duastore.repository;

import com.duastore.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu liên hệ.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    Optional<ContactMessage> findById(Integer id);
}