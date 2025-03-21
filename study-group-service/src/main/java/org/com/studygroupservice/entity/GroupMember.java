package org.com.studygroupservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.com.studygroupservice.enums.GroupMemberRole;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GroupMemberRole role = GroupMemberRole.MEMBER;

    public GroupMember(Long id, Long accountId, StudyGroup studyGroup) {
        this.id = id;
        this.accountId = accountId;
        this.studyGroup = studyGroup;
        this.role = GroupMemberRole.MEMBER;
    }
}