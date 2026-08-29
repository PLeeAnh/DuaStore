package com.duastore.repository;

import com.duastore.model.ContactReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn phản hồi tin nhắn liên hệ.
 */
@Repository
public interface ContactReplyRepository extends JpaRepository<ContactReply, Integer> {

    List<ContactReply> findByContactIdOrderByCreatedAtAsc(Integer contactId);

    long countByContactId(Integer contactId);
}
