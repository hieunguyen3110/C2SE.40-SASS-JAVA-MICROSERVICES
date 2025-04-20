package org.com.elearningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assessment extends AbstractDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(name = "online_courses_completed")
    private Integer onlineCoursesCompleted;

    @Column(name = "online_test_completed")
    private Integer onlineTestCompleted;

    @Column(name = "participation_in_discussions")
    private Boolean participationInDiscussions;

    @Column(name = "assignment_completion_rate")
    private Integer assignmentCompletionRate;

    @Column(name = "exam_score")
    private Integer examScore;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quiz> quizzes;

}
