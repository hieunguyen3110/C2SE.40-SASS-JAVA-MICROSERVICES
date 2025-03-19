package org.com.studygroupservice.dto.request;


import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ShareDocumentRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String documentId;

    @NotNull
    private String shareUrl;
}
