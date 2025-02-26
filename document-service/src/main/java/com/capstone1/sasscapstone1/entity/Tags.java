package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="Tags")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private long tagId;

    @Column(name = "tag_name")
    private String tagName;

    @OneToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<DocTag> docTags= new HashSet<>();
}
