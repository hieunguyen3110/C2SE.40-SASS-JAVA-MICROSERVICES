package com.capstone1.sasscapstone1.repository.AccountRole;

import com.capstone1.sasscapstone1.entity.AccountRole;
import com.capstone1.sasscapstone1.entity.AccountRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, AccountRoleId> {
    @Query("SELECT ar.role.name FROM AccountRole ar WHERE ar.account.accountId = :accountId")
    List<String> findRoleNamesByAccountId(@Param("accountId") Long accountId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AccountRole ar WHERE ar.account.accountId = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);
}
