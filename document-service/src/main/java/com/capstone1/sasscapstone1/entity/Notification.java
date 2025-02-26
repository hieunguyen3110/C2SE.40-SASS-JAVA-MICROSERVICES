package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Notification")
public class Notification extends AbstractDefault{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String message;

    private String type; // Loại thông báo

    @Column(name = "is_saved", columnDefinition = "boolean default false")
    private Boolean isSaved;

    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead;

    @Column(name = "deleted_flag", columnDefinition = "boolean default false")
    private Boolean deletedFlag;

}
