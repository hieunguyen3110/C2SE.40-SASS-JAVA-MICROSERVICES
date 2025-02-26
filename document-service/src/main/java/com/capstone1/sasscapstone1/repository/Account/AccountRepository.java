package com.capstone1.sasscapstone1.repository.Account;

import com.capstone1.sasscapstone1.dto.AccountStatisticsDto.AccountStatisticsDto;
import com.capstone1.sasscapstone1.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findAccountByEmail(String email);
    Optional<Account> findAccountByEmailAndIsActive(String email, boolean active);
    List<Account> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    long countByRoles_Name(String roleName);

//    @Query("SELECT a FROM Account a JOIN a.roles r WHERE r.name IN ('LECTURER', 'STUDENT') AND a.isDeleted = false")
//    Page<Account> findByRoleLecturerOrStudent(Pageable pageable);

    @Query("select a from Account a where " +
            "(lower(a.firstName) like lower(concat('%',:firstName,'%')) or " +
            "lower(a.lastName) like  lower(concat('%',:lastName,'%'))) and " +
            "a.isActive=true and " +
            "a.accountId !=:accountId and " +
            "not exists (select r from a.roles r where r.name='ADMIN')")
    Page<Account> findAccountByFirstNameAndLastName(@Param("firstName") String firstName,
                                                    @Param("lastName") String lastName,
                                                    @Param("accountId") Long accountId,
                                                    Pageable pageable);

    Page<Account> findByFirstNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT new com.capstone1.sasscapstone1.dto.AccountStatisticsDto.AccountStatisticsDto(COUNT(DISTINCT f1.follower.accountId),COUNT(DISTINCT f2.following.accountId),COUNT(DISTINCT d.docId)) " +
            "FROM Account a " +
            "LEFT JOIN a.followers f1 " +
            "LEFT JOIN a.following f2 " +
            "LEFT JOIN a.documents d " +
            "WHERE a.accountId = :accountId and d.isActive=true")
    AccountStatisticsDto getAccountStatistics(@Param("accountId") Long accountId);
}
