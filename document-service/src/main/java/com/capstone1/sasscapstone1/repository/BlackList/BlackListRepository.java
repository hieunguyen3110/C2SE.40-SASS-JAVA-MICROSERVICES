package com.capstone1.sasscapstone1.repository.BlackList;

import com.capstone1.sasscapstone1.entity.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList,Long> {
    Optional<BlackList> findBlackListsByToken(String token);
}
