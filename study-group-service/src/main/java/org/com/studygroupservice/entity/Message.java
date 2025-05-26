package org.com.studygroupservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.studygroupservice.enums.MessageType;

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
    private String documentName;
    private String docFilePath;
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudyGroup group;
}