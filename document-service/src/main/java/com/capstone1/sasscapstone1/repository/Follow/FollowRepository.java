package com.capstone1.sasscapstone1.repository.Follow;

import com.capstone1.sasscapstone1.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.capstone1.sasscapstone1.entity.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    @Query("select f.follower.accountId from Follow f where f.following.accountId=:followingId")
    List<Long> findAllFollowerByFollowingId(long followingId);

    List<Follow> findByFollower(Account account);

    List<Follow> findByFollowing(Account account);

    boolean existsByFollowerAndFollowing(Account follower, Account following);

    Optional<Follow> findByFollowerAndFollowing(Account follower, Account following);

}
