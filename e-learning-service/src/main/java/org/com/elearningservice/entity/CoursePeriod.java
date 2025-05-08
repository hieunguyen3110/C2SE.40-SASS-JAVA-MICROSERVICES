package org.com.elearningservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.elearningservice.config.JsonToMapConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_period")
public class CoursePeriod extends AbstractDefault{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_period_id")
    private Long coursePeriodId;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    @Convert(converter = JsonToMapConverter.class)
    @Column(columnDefinition = "text")
    private Map<Long, String> subjects;
    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
