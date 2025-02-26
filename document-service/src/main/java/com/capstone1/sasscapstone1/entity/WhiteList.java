package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "White_list")
public class WhiteList extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "white_list_id")
    private long whiteListId;
    private String token;
    @Column(name = "expiration_token", columnDefinition = "timestamp")
    private LocalDateTime expirationToken;
    @Column(name = "account_id",length = 100)
    private long accountId;
}
