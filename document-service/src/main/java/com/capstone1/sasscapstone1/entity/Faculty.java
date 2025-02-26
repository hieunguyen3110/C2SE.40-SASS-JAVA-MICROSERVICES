package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Faculty")
public class Faculty extends AbstractDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facultyId;

    @Column(name = "faculty_name", nullable = false)
    private String facultyName;

    @OneToMany(mappedBy = "faculty",fetch = FetchType.LAZY)
    private Set<Account> accounts;

}
