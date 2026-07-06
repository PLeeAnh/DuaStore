package com.duastore.repository;

import com.duastore.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    List<Banner> findAllByOrderByDisplayOrderAscCreatedAtDesc();

    @Query("""
        select b from Banner b
        where b.active = true
          and (b.startDate is null or b.startDate <= :now)
          and (b.endDate is null or b.endDate >= :now)
        order by b.displayOrder asc, b.createdAt asc
        """)
    List<Banner> findActiveForDisplay(@Param("now") LocalDateTime now);
}
