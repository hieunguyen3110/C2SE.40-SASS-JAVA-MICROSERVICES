package org.com.batchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyStudyPlan {
    private List<String> long_term_goals;
    private List<String> short_term_goals;
    private Map<String, WeeklyStudyPlanItem> daily_schedule;
}
