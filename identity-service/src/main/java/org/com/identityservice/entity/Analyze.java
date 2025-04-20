package org.com.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analyze_data")
public class Analyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analyze_id")
    private Long analyzeId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;
}
