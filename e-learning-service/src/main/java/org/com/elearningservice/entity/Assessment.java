package org.com.elearningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    private Integer studyHoursPerWeek;
    private String preferredLearningStyle;
    private Integer onlineCoursesCompleted;
    private Boolean participationInDiscussions;
    private Integer assignmentCompletionRate;
    private Integer examScore;
    private Integer timeSpentOnSocialMedia;
    private Integer sleepHoursPerNight;
}
