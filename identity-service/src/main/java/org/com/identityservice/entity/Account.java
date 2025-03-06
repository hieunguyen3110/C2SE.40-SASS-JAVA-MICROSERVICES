package org.com.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name="Account")
public class Account extends AbstractDefault implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "password",nullable = false, length = 150)
    private String password;

    @Column(name = "email",unique = true,nullable = false, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "hometown")
    private String hometown;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "major")
    private String major;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(name = "class")
    private String classNumber;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinTable(
            name = "Account_Role",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnoreProperties("accounts")
    private Set<Role> roles;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities= new ArrayList<>();
        for(Role role: roles){
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getName()));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public String getPassword(){
        return password;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
