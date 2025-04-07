package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Ratings")
public class Ratings extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private long rateId;

    @Column(name = "rating")
    private int rating;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "view_time")
    private float viewTime;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doc_id")
    private Documents documents;

    @Column(name = "account_id")
    private long accountId;

    @OneToMany(mappedBy = "ratings", fetch = FetchType.LAZY)
    private Set<Feedbacks> feedbacks= new HashSet<>();
}
