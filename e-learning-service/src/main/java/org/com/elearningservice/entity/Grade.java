package org.com.elearningservice.entity;

import lombok.*;
import jakarta.persistence.*;
import org.com.elearningservice.enums.ResultType;
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
    private Long id;
    private Long accountId;
    private Long subjectId;
    private Float score;
    private int totalQuestions;
    @Enumerated(EnumType.STRING)
    private ResultType type;

}
