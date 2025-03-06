package org.com.notificationservice.entity;

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

    @Column(name = "account_id", nullable = false)
    private long accountId;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "is_saved", columnDefinition = "boolean default false", nullable = false)
    private Boolean isSaved=false;

    @Column(name = "is_read", columnDefinition = "boolean default false", nullable = false)
    private Boolean isRead=false;

    @Column(name = "deleted_flag", columnDefinition = "boolean default false", nullable = false)
    private Boolean deletedFlag=false;

}
