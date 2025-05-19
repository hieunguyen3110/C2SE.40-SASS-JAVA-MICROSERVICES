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
public class ProgressTracking {
    private List<String> adjustment_strategies;
    private List<String> metrics_to_monitor;
}
