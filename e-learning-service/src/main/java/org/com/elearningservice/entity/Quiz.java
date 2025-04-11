package org.com.elearningservice.entity;

import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "test_duration")
    private Long testDuration;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Grade> grades;

}
