package rb.ebooklib.controllers;

import jakarta.validation.Valid;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rb.ebooklib.payload.request.LoginRequest;
import rb.ebooklib.payload.request.SignupRequest;
import rb.ebooklib.payload.response.MessageResponse;
import rb.ebooklib.repositories.UserRepository;
import rb.ebooklib.security.jwt.JwtUtils;
import rb.ebooklib.services.UserService;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        val user = userService.authenticateUser((loginRequest));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        val user = userService.signupUser(signUpRequest);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

}
