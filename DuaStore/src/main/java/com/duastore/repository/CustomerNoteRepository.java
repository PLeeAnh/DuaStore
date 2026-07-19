package com.duastore.repository;

import com.duastore.model.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Integer> {

    List<CustomerNote> findByUserIdOrderByCreatedAtDesc(Integer userId);

    long countByUserId(Integer userId);

    void deleteByIdAndUserId(Integer id, Integer userId);
}
