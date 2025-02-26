package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "History")
public class History extends AbstractDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Documents document;

    @Column(name = "download_count", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int downloadCount;

    public History() {

    }

    // Constructor nhận `Documents`
    public History(Documents document, int downloadCount) {
        this.document = document;
        this.downloadCount = downloadCount;
    }
}
