package rb.ebooklib.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import rb.ebooklib.models.User;

import java.util.Collection;

public class CustomerUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private User user;

    public CustomerUserDetails(User user) {
        this.user = user;
    }

    public static CustomerUserDetails build(User user) {
        return new CustomerUserDetails(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
