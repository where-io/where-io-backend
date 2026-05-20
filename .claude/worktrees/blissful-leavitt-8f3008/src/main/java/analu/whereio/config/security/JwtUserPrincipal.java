package analu.whereio.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class JwtUserPrincipal implements UserDetails {

    private final String userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserPrincipal(String userId, String email, Collection<String> roles) {
        this.userId = userId;
        this.email = email;
        this.authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    }

    public static JwtUserPrincipal testPrincipal(String userId) {
        return new JwtUserPrincipal(userId, userId + "@test.local", List.of("USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return "";
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
