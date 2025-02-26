package com.capstone1.sasscapstone1.repository.WhiteList;

import com.capstone1.sasscapstone1.entity.WhiteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhiteListRepository extends JpaRepository<WhiteList,Long> {
    Optional<WhiteList> findByAccountId(long accountId);
    Optional<WhiteList> findWhiteListByToken(String token);
}
