package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Saved_Documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id", "doc_id"})})
public class SavedDocuments extends AbstractDefault{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_id")
    private Long savedId;

    @Column(name = "account_id")
    private long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Documents document;
}
