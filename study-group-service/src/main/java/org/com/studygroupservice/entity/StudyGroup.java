package org.com.studygroupservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "study_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup extends AbstractDefault{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String picture;
    private int memberLimited;

    @Column(name = "subject_id")
    private Long subjectId;

    @JsonIgnore
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroupMember> members;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Message> messages;

    @Column(nullable = false)
    private Boolean isPrivate;

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinRequest> joinRequests = new ArrayList<>();
}
