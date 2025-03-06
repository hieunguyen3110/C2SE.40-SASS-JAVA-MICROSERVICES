package com.capstone1.sasscapstone1.repository.Follow;

import com.capstone1.sasscapstone1.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    @Query("select f.followerId from Follow f where f.followingId=:followingId")
    List<Long> findAllFollowerByFollowingId(long followingId);

    List<Follow> findByFollowerId(Long followerId);

    List<Follow> findByFollowingId(Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("select COUNT(DISTINCT f.followingId) from Follow f where f.followerId=:accountId")
    Long countFollowingByAccountId(Long accountId);
    @Query("select COUNT(DISTINCT f.followerId) from Follow f where f.followingId=:accountId")
    Long countFollowerByAccountId(Long accountId);

}
