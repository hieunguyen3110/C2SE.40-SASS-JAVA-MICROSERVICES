package com.capstone1.sasscapstone1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name="Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private long roleId;

    @Column(name = "name", nullable = false,unique = true,length = 50)
    private String name;

//    @OneToMany(fetch = FetchType.EAGER, mappedBy = "role")
//    private Set<AccountRole> accountRoles= new HashSet<>();

    @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<Account> accounts= null;
}
