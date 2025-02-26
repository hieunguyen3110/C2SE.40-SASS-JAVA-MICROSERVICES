package com.capstone1.sasscapstone1.repository.Tags;

import com.capstone1.sasscapstone1.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends JpaRepository<Tags,Long> {
}
