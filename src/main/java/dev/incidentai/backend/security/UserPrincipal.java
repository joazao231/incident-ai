package dev.incidentai.backend.security;

import dev.incidentai.backend.entity.UserAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public record UserPrincipal(UserAccount user) implements UserDetails {
    @Override public Collection<SimpleGrantedAuthority> getAuthorities() { return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isEnabled() { return user.isEnabled(); }
}
