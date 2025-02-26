package com.capstone1.sasscapstone1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AccountRoleId implements Serializable {
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "role_id")
    private Long roleId;
}
