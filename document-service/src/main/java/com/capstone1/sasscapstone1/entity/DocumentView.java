package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_view")
@Entity
public class DocumentView extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_view_id")
    private Long docViewId;
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = "document_id")
    private Long documentId;
    @Column(name = "last_view_time")
    private LocalDateTime startViewTime;
    @Column(name = "duration_seconds")
    private long durationSeconds;
}
