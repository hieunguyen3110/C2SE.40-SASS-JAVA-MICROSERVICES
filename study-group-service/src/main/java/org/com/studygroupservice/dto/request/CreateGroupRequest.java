package org.com.studygroupservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message = "Group name cannot be empty")
    private String groupName;

    @NotNull(message = "Member list cannot be null.")
    private List<Long> memberIds;

    private String description;

    private boolean isPrivate;
    private int memberLimited;
    private Long subjectId;
    private String picture;
}