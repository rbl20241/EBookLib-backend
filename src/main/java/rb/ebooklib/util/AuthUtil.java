package rb.ebooklib.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import rb.ebooklib.security.service.CustomerUserDetails;
import rb.ebooklib.services.AuthenticationService;

@Component
public class AuthUtil {

    @Autowired
    private AuthenticationService authenticationService;

    public Long getAuthenticatedUserId() {
        // thread safe way of retrieving the currently logged in user-id
        //final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Authentication authentication = authenticationService.getAuthenticationManager();
        final CustomerUserDetails userDetails = (CustomerUserDetails) authentication.getPrincipal();

        return userDetails.getId();
    }

    public void logOut(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = authenticationService.getAuthenticationManager();
//        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
    }

}
