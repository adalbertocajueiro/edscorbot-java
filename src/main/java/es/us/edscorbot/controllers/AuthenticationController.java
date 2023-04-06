package es.us.edscorbot.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.jwt.CustomAuthenticationProvider;
import es.us.edscorbot.jwt.JwtTokenUtil;
import es.us.edscorbot.jwt.JwtUserDetailsService;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.GlobalPasswordEncoder;
import es.us.edscorbot.util.LoginRequest;
import es.us.edscorbot.util.LoginResponse;
import es.us.edscorbot.util.Role;
import es.us.edscorbot.util.SignupRequest;
import es.us.edscorbot.util.UserBuilder;
import es.us.edscorbot.util.UserRole;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private CustomAuthenticationProvider authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest credentials) throws Exception {

        authenticate(credentials.getUsername(), credentials.getPassword());

       final UserDetails userDetails = userDetailsService
                .loadUserByUsername(credentials.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        User user = new User();
        LoginResponse response = new LoginResponse();
        
        if(!credentials.getUsername().equals("root")){
            user = this.userRepository.findById(credentials.getUsername()).get();
            
        } else {
            user = UserBuilder.rootUser();
        }

        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setEnabled(user.isEnabled());
        response.setName(user.getName());
        response.setRole(user.getRole().getRoleName());
        response.setToken(token);
        

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody SignupRequest body) {
        try {
            User user = new User();
            user.setUsername(body.getUsername());
            PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
            user.setPassword(pe.encode(body.getPassword()));
            user.setName(body.getName());
            user.setEnabled(false);
            user.setEmail(body.getEmail());
            user.setRole(new Role(UserRole.USER));
            
            Optional<User> found = this.userRepository.findById(user.getUsername());
            if (found.isPresent()) {
                return new ResponseEntity<String>("User already registered!", HttpStatus.PRECONDITION_FAILED);
            } else {
                this.userRepository.save(user);
                return ResponseEntity.ok().body(user);
            }    
        } catch (Exception e) {
            return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
