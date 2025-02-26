package com.capstone1.sasscapstone1.repository.Feedbacks;

import com.capstone1.sasscapstone1.entity.Feedbacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbacksRepository extends JpaRepository<Feedbacks,Long> {
}
