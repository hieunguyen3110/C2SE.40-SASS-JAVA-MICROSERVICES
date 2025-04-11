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

    @Column(name = "study_hours_per_week")
    private Integer studyHoursPerWeek;

    @Column(name = "preferred_learning_style")
    private String preferredLearningStyle;

    @Column(name = "online_courses_completed")
    private Integer onlineCoursesCompleted;

    @Column(name = "participation_in_discussions")
    private Boolean participationInDiscussions;

    @Column(name = "assignment_completion_rate")
    private Integer assignmentCompletionRate;

    @Column(name = "time_spent_on_social_media")
    private Integer timeSpentOnSocialMedia;

    @Column(name = "sleep_hours_per_night")
    private Integer sleepHoursPerNight;

    @Column(name = "exam_score")
    private Integer examScore;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quiz> quizzes;

}
