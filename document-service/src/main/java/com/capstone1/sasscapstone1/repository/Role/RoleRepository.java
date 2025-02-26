package com.capstone1.sasscapstone1.repository.Role;

import com.capstone1.sasscapstone1.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findRoleByName(String roleName);
}
