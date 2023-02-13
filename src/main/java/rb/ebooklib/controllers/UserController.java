package rb.ebooklib.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rb.ebooklib.models.User;
import rb.ebooklib.services.UserService;

/**
 * In a restController annotated controller it is not needed to annotate with @ResponseBody annotation
 * since it's done by default.
 */
@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

//    @PostMapping
//    public ResponseEntity<User> registerUser(@RequestBody final UserDTO userDTO) {
//        User user = userService.registerUser(userDTO);
//        return new ResponseEntity<>(user, HttpStatus.OK);
//    }

    @GetMapping("/currentuserid")
    public ResponseEntity<Long> getUserIdCurrentUser() {
        final Long id = userService.getCurrentUserId();
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping("/currentuser")
    public ResponseEntity<User> getCurrentUser() {
        final Long userId = userService.getCurrentUserId();
        final User user = userService.findUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutPage(final HttpServletRequest request, final HttpServletResponse response) {
        userService.logOut(request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
