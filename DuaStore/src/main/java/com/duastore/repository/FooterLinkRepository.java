package com.duastore.repository;

import com.duastore.model.FooterLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FooterLinkRepository extends JpaRepository<FooterLink, Integer> {

    List<FooterLink> findAllByOrderByColumnIndexAscDisplayOrderAsc();
}
