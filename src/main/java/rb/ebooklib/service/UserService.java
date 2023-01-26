package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.model.User;
import rb.ebooklib.payload.AuthResponse;
import rb.ebooklib.payload.LoginRequest;
import rb.ebooklib.payload.RegisterRequest;
import rb.ebooklib.persistence.UserRepository;
import rb.ebooklib.security.TokenProvider;
import rb.ebooklib.util.AuthUtil;
import rb.ebooklib.util.ViewObjectMappers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class UserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value( "${app.auth.tokenExpirationMsec}" )
    private long tokenExpirationMsec;


    /**
     * authenticate the client logging in to the application
     *
     * @param loginRequest the email and password provided by the client
     * @return the accesstoken
     */
    public AuthResponse authenticateUser(final LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = tokenProvider.createToken(authentication);
        final AuthResponse response = new AuthResponse(token);
        response.setTokenExpirationMsec(tokenExpirationMsec);
        return response;
    }

    /**
     * Registration of a new user
     *
     * @param registerRequest the user to register
     * @return a responseEntity to return to the API
     */
    @Transactional
    public User registerUser(final RegisterRequest registerRequest) {
        //check if email and password are filled
        if (isNullOrEmpty(registerRequest.getUsername()) || isNullOrEmpty(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Gebruikersnaam en emailadres zijn verplicht");
        }

        //check if user already exists
        if (userRepository.findByUsernameIgnoreCase(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Deze gebruiker bestaat al.");
        }

        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        return userRepository.save(viewObjectMappers.convertRegisterRequestToUser(registerRequest));
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
        final Optional<User> user = userRepository.findById(getCurrentUserId());
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
