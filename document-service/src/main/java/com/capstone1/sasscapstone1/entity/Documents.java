package com.capstone1.sasscapstone1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Documents")
public class Documents extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private long docId;

    private String title;

    private String description;

    private String content;

    private String type;

    @Column(name="file_path")
    private String filePath;

    @Column(name = "file_size", columnDefinition = "numeric DEFAULT 1")
    private int fileSize;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    private Subject subject;

    @Column(name = "is_active", columnDefinition = "boolean DEFAULT false")
    private Boolean isActive;

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @Column(name = "is_Check", nullable = false)
    private Boolean isCheck = false;

    @Column(name = "is_train")
    private Boolean isTrain= false;

    @Column(name = "approved_by")
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "folder_id")
    @JsonIgnore
    private Folder folder;

    @OneToMany(mappedBy = "documents", fetch = FetchType.LAZY)
    private Set<DocTag> docTags= new HashSet<>();

    @OneToMany(mappedBy = "documents", fetch = FetchType.LAZY)
    private Set<Versions> versions= new HashSet<>();

    @OneToMany(mappedBy = "documents", fetch = FetchType.LAZY)
    private Set<Feedbacks> feedbacks= new HashSet<>();
}
