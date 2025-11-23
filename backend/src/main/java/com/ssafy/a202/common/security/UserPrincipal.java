package com.ssafy.a202.common.security;

import com.ssafy.a202.domain.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // JWT 기반 인증이므로 패스워드 불필요
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }
}