package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.dto.UserDTO;
import rb.ebooklib.model.User;
import rb.ebooklib.persistence.UserRepository;
import rb.ebooklib.util.AuthUtil;
import rb.ebooklib.util.ViewObjectMappers;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ViewObjectMappers viewObjectMappers;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Registration of a new user
     *
     * @param userDTO the user to register
     * @return a responseEntity to return to the API
     */
    @Transactional
    public User registerUser(final UserDTO userDTO) {
        //check if email and password are filled
        if (isNullOrEmpty(userDTO.getUsername()) || isNullOrEmpty(userDTO.getEmail())) {
            throw new IllegalArgumentException("Gebruikersnaam en emailadres zijn verplicht");
        }

        //check if user already exists
        if (userRepository.findByUsernameIgnoreCase(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Deze gebruiker bestaat al.");
        }

        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        return userRepository.save(viewObjectMappers.convertUserDTOToUser(userDTO));
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
