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
public class DocumentView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_view_id")
    private Long docViewId;
    @Column(name = "account_id", unique = true)
    private Long accountId;
    @Column(name = "document_id", unique = true)
    private Long documentId;
    @Column(name = "last_view_time")
    private LocalDateTime lastViewTime;
}
