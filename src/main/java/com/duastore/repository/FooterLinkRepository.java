package com.duastore.repository;

import com.duastore.model.FooterLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu liên kết ở footer.
 */
public interface FooterLinkRepository extends JpaRepository<FooterLink, Integer> {

    List<FooterLink> findAllByOrderByColumnIndexAscDisplayOrderAsc();
}
