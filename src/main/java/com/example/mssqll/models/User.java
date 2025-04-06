package com.example.mssqll.models;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FilterDef(name = "activeFilter", parameters = @ParamDef(name = "role", type = String.class))
@Filter(name = "activeFilter", condition = "role != :role")
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email")
        }
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Nationalized
    String firstName;

    @Nationalized
    String lastName;

    @Column(unique = true)
    String email;

    String password;

    @Enumerated(EnumType.STRING)
    Role role;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
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