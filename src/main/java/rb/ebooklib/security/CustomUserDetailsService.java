package rb.ebooklib.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rb.ebooklib.model.User;
import rb.ebooklib.persistence.UserRepository;
import rb.ebooklib.user.SecurityUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    private final Map<String, SecurityUser> userMap = new HashMap<>();

    public CustomUserDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        userMap.put("user", createUser("user", bCryptPasswordEncoder.encode("userPass"), false, "USER"));
        userMap.put("admin", createUser("admin", bCryptPasswordEncoder.encode("adminPass"), true, "ADMIN", "USER"));
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Gebruiker " + username + " bestaat niet"));
        return UserPrincipal.create(user);
    }

    private SecurityUser createUser(String userName, String password, boolean withRestrictedPolicy, String... role) {
        return SecurityUser.builder()
                .withUserName(userName)
                .withPassword(password)
                .withGrantedAuthorityList(Arrays.stream(role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .withAccessToRestrictedPolicy(withRestrictedPolicy);
    }

}
