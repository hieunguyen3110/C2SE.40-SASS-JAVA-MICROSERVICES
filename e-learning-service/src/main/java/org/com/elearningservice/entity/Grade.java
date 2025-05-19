package org.com.elearningservice.entity;

import lombok.*;
import jakarta.persistence.*;
import org.com.elearningservice.enums.ResultType;

import java.util.List;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long id;
    private Long accountId;
    private Long subjectId;
    private Long docId;
    private Float score;
    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    private ResultType type;

    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GradeQuestion> gradeQuestions;

}
