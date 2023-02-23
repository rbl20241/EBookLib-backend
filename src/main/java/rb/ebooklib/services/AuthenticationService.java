package rb.ebooklib.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private UsernamePasswordAuthenticationToken authentication;

    public void setAuthenticationManager(final UsernamePasswordAuthenticationToken authentication) {
        this.authentication = authentication;
    }

    public UsernamePasswordAuthenticationToken getAuthenticationManager() {
        return this.authentication;
    }
}
