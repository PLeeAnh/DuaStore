package com.duastore.repository;

import com.duastore.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    List<Supplier> findByIsActiveTrueOrderByTenNhaCungCapAsc();
    List<Supplier> findByTenNhaCungCapContainingIgnoreCaseOrderByTenNhaCungCapAsc(String name);
}
