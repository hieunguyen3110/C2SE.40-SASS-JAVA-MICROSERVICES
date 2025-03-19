package org.com.studygroupservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareDocumentRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Document ID cannot be empty")
    private String documentId;

    @NotBlank(message = "Share URL cannot be empty")
    private String shareUrl;
}