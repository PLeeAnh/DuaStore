package com.duastore.repository;

import com.duastore.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu vai trò (role).
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByName(String name);
}
