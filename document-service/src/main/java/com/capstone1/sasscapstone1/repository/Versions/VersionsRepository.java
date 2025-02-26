package com.capstone1.sasscapstone1.repository.Versions;

import com.capstone1.sasscapstone1.entity.Versions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionsRepository extends JpaRepository<Versions,Long> {
}
