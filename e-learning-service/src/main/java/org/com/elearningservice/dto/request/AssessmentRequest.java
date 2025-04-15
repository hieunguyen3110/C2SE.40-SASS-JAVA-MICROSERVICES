package org.com.elearningservice.dto.request;

import lombok.Data;

@Data
public class AssessmentRequest {
    private Integer studyHoursPerWeek;
    private String preferredLearningStyle;
    private Integer onlineCoursesCompleted;
    private Boolean participationInDiscussions;
    private Integer assignmentCompletionRate;
    private Integer timeSpentOnSocialMedia;
    private Integer sleepHoursPerNight;
}
