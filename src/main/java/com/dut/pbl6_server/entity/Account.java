package com.dut.pbl6_server.entity;

import com.dut.pbl6_server.common.model.AbstractEntity;
import com.dut.pbl6_server.entity.enums.AccountGender;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.entity.enums.AccountStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "accounts")
public class Account extends AbstractEntity implements UserDetails {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccountRole role;

    @Column(nullable = false, unique = true)
    private String displayName;

    private Timestamp birthday;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AccountGender gender;

    private String bio;

    @OneToOne
    @JoinColumn(name = "avatar", unique = true)
    private File avatarFile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Column(nullable = false)
    @Builder.Default
    private String language = "vi";

    //
    // Relationships
    //
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Follower> followers; // 'followers' are the users who follow this user

    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    private List<Follower> followingUsers; // 'followingUsers' are the users whom this user is following

    //
    // UserDetails methods
    //
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

    // Account is active or inactive
    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
