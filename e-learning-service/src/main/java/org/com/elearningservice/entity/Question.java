package org.com.elearningservice.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question extends AbstractDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    private String content;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(name = "correct_answer")
    private String correctAnswer;
}
//content: "Question content",
//options: {
//        "A": "Option A",
//        "B": "Option B",
//        "C": "Option C",
//        }
//correctAnswer: ["A", "B"]

