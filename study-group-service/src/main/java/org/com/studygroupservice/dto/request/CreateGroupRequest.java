package org.com.studygroupservice.dto.request;

import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    private String description;
}
