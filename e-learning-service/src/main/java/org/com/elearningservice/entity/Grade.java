package org.com.elearningservice.entity;

import lombok.*;
import jakarta.persistence.*;
import org.com.elearningservice.enums.ResultType;

import java.util.List;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long subjectId;
    private int score;
    private int totalQuestions;

    @Enumerated(EnumType.STRING)
    private ResultType type;

}
