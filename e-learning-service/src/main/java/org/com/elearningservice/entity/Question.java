package org.com.elearningservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.elearningservice.config.JsonToMapConverter;

import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;
    private Long subjectId;
    private String questionText;
    private String correctAnswer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GradeQuestion> gradeQuestions;
    @Convert(converter = JsonToMapConverter.class)
    @Column(columnDefinition = "text")
    private Map<String, Object> options;
}

