package org.com.studygroupservice.dto.response;

import lombok.Data;
import org.com.studygroupservice.entity.StudyGroup;

import java.util.List;

@Data
public class SearchGroupResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private String subjectName;
    private int memberCount;
    private String picture;
    List<Long> memberIds;
}
