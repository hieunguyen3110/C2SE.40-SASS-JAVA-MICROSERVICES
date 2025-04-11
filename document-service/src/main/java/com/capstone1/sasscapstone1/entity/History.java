package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "History")
@NoArgsConstructor
@AllArgsConstructor
public class History extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Documents document;

    @Column(name = "click_count")
    private int clickCount;

    @Column(name = "download_count", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int downloadCount;

    @Column(name = "average_rating")
    private float averageRating;
}
