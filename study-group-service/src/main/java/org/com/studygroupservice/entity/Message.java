package org.com.studygroupservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private String content;
    private Boolean isPinned;
    private Boolean documentLink;
    private String documentId;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudyGroup group;
}