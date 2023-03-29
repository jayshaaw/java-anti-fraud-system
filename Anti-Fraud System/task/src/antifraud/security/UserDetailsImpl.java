package antifraud.security;

import antifraud.model.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;

    private final String operation;
    private final List<GrantedAuthority> rolesAndAuthorities;


    public UserDetailsImpl(Users users) {
        username = users.getUsername();
        password = users.getPassword();
        rolesAndAuthorities = List.of(new SimpleGrantedAuthority("ROLE_" + users.getRole()));
        operation = users.getOperation();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAndAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (Objects.equals(operation, "LOCK")) {
            return false;
        } else {
            return true;
        }

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
