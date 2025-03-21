package org.com.studygroupservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}
