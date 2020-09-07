package rb.ebooklib.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.dto.UserDTO;
import rb.ebooklib.model.User;
import rb.ebooklib.model.view.ResetPasswordRequestVO;
import rb.ebooklib.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
