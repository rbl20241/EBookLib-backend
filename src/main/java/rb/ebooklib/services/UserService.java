package rb.ebooklib.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.models.User;
import rb.ebooklib.payload.AuthResponse;
import rb.ebooklib.payload.request.LoginRequest;
import rb.ebooklib.payload.request.SignupRequest;
import rb.ebooklib.repositories.UserRepository;
import rb.ebooklib.security.jwt.JwtUtils;
import rb.ebooklib.util.AuthUtil;
import rb.ebooklib.util.ViewObjectMappers;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class UserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationService authenticationService;

    @Value( "${app.auth.jwtExpirationMsec}" )
    private long tokenExpirationMsec;


    /**
     * authenticate the client logging in to the application
     *
     * @param loginRequest the email and password provided by the client
     * @return the accesstoken
     */
    public AuthResponse authenticateUser(final LoginRequest loginRequest) {

        val authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        //SecurityContextHolder.getContext().setAuthentication(authentication);
        authenticationService.setAuthenticationManager((UsernamePasswordAuthenticationToken) authentication);

        val token = jwtUtils.generateToken(authentication);
        val response = new AuthResponse(token);
        response.setTokenExpirationMsec(tokenExpirationMsec);
        return response;
    }

    /**
     * Registration of a new user
     *
     * @param signupRequest the user to register
     * @return a responseEntity to return to the API
     */
    @Transactional
    public User signupUser(final SignupRequest signupRequest) {
        if (isNullOrEmpty(signupRequest.getUsername()) || isNullOrEmpty(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Gebruikersnaam en emailadres zijn verplicht!");
        }

        if (Boolean.TRUE.equals(userRepository.existsByUsernameIgnoreCase(signupRequest.getUsername()))) {
            throw new IllegalArgumentException("Gebruikersnaam wordt al gebruikt!");
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(signupRequest.getEmail()))) {
            throw new IllegalArgumentException("Emailadres wordt al gebruikt!");
        }

        signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        return userRepository.save(viewObjectMappers.convertSignupRequestToUser(signupRequest));
    }

    /**
     * Find an User by an Id
     *
     * @param userId the Id of the user
     * @return The user object if it was found
     */
    public User findUserById(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId));
    }

    /**
     * Retrieve the userId of the currently logged in user.
     *
     * @return The id
     */
    public Long getCurrentUserId() {
        return authUtil.getAuthenticatedUserId();
    }

    public User getCurrentlyLoggedInUser() {
        val user = userRepository.findById(getCurrentUserId());
        return user.orElseThrow(() -> new RuntimeException("No current USER !!!!"));
    }

    /**
     * Reset the password of the currently logged in user.
     *
     * @param request ResetPasswordRequestVO object with the old and the new password.
     */
//    @Transactional
//    public void resetPassword(final ResetPasswordRequestVO request) {
//        final Long currentUserId = this.getCurrentUserId();
//        final User user = userRepository.findById(currentUserId)
//                .orElseThrow(() -> new EntityNotFoundException("No user with id " + currentUserId));
//
//        if (!passwordEncoder.matches(request.getPreviousPassword(), user.getPassword())) {
//            throw new IllegalArgumentException("Entered wrong previous password");
//        }
//
//        if (!request.getNewPassword().equals(request.getNewPasswordConfirmation())) {
//            throw new IllegalArgumentException("New passwords do not match");
//        }
//
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//    }

    public void logOut(final HttpServletRequest request, final HttpServletResponse response) {
        authUtil.logOut(request, response);
    }

}
