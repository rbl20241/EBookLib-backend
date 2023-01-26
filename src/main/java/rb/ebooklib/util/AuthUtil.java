package rb.ebooklib.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import rb.ebooklib.security.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthUtil {

    public Long getAuthenticatedUserId() {
        // thread safe way of retrieving the currently logged in user-id
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return principal.getId();
    }

    public void logOut(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
    }

}
