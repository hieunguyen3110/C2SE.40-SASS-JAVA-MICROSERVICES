package com.capstone1.sasscapstone1.repository.Ratings;

import com.capstone1.sasscapstone1.entity.Ratings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingsRepository extends JpaRepository<Ratings,Long> {
}
