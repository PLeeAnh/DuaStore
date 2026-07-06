package com.duastore.repository;

import com.duastore.model.StoreInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInfoRepository extends JpaRepository<StoreInfo, Integer> {
    Optional<StoreInfo> findByIsDefaultTrueAndIsActiveTrue();
    List<StoreInfo> findByIsActiveTrueOrderByIdAsc();
}
