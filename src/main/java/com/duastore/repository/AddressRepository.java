package com.duastore.repository;

import com.duastore.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu địa chỉ giao hàng.
 */
public interface AddressRepository extends JpaRepository<Address, Integer> {

    long countByUserId(Integer userId);

    long countByUserIdAndTenNguoiNhanAndSoDienThoaiAndTinhThanhAndQuanHuyenAndPhuongXaAndDiaChiCuTheAndIdNot(
            Integer userId, String tenNguoiNhan, String soDienThoai, String tinhThanh, String quanHuyen, String phuongXa, String diaChiCuThe, Integer id);

    @Query("SELECT DISTINCT a.tinhThanh FROM Address a WHERE a.tinhThanh IS NOT NULL AND a.tinhThanh <> '' ORDER BY a.tinhThanh")
    List<String> findAllDistinctCities();

    @Query("SELECT a FROM Address a WHERE a.userId = ?1 ORDER BY a.isDefault DESC, a.id DESC")
    List<Address> findByUserIdOrderByIsDefaultDesc(Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = ?1")
    void clearDefaultAddressByUserId(Integer userId);
}
