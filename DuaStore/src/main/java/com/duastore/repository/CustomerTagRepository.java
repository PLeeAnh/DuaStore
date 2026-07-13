package com.duastore.repository;

import com.duastore.model.CustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTag, Integer> {

    List<CustomerTag> findByUserId(Integer userId);

    @Query("SELECT DISTINCT t.tag FROM CustomerTag t ORDER BY t.tag")
    List<String> findDistinctTags();

    long countByUserId(Integer userId);

    void deleteByIdAndUserId(Integer id, Integer userId);
}
