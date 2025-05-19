package org.com.batchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyStudyPlanItem {
    private List<String> focus_subjects;
    private List<String> recommended_activities;
    private String study_hours;
}
