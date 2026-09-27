package vn.edu.docucatalog.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.docucatalog.domain.UserAccount;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String roleLabel;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(UserAccount account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.password = account.getPassword();
        this.fullName = account.getFullName();
        this.roleLabel = account.getRole().getLabel();
        this.active = account.isActive();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getRoleLabel() { return roleLabel; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
