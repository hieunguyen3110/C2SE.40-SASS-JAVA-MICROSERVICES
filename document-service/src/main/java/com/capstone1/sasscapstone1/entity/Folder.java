package com.capstone1.sasscapstone1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name="Folder")
public class Folder extends AbstractDefault {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private long folderId;

    @Column(name = "folder_name",nullable = false)
    private String folderName;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    private Set<Documents> documents= new HashSet<>();

    @OneToMany(mappedBy = "folderId", fetch = FetchType.LAZY)
    private Set<DocumentShares> documentShares = new HashSet<>();

    @Transient
    private long documentCount;

}
