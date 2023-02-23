package rb.ebooklib.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import rb.ebooklib.security.service.CustomUserDetailsService;
import rb.ebooklib.services.AuthenticationService;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationService authenticationService;

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {
                // Long userId = tokenProvider.getUserIdFromToken(jwt);
                val userName = jwtUtils.getUserNameFromJwtToken(jwt);

                val userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                SecurityContextHolder.getContext().setAuthentication(authentication);
                authenticationService.setAuthenticationManager(authentication);

            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        return jwtUtils.getJwtFromCookies(request);
    }
}
