package es.us.edscorbot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.jwt.AuthenticationException;
import es.us.edscorbot.jwt.JwtTokenUtil;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.ApplicationError;
import es.us.edscorbot.util.ErrorDTO;
import es.us.edscorbot.util.GlobalPasswordEncoder;
import es.us.edscorbot.util.Role;
import es.us.edscorbot.util.UserDTO;
import es.us.edscorbot.util.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "usertoken") String userToken) {
        try {
            String username = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + username);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
            Optional<User> found = this.userRepository.findById(username);
            if (found.isPresent()) {
                List<User> users = this.userRepository.findAll();
                User user = found.get();
                if (user.getRole().getRoleName().equals(UserRole.USER)) {
                    users.removeIf(u -> !u.getUsername().equals(userToken));
                }
                return ResponseEntity.ok().body(users);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Logged user not found: " + username);
                error.setDetailedMessage("Logged user not found: " + username);
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch( UnsupportedJwtException | MalformedJwtException 
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {
            
            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping(value = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String username,
            @RequestHeader(value = "usertoken") String userToken) {

        try {
            String loggedUsername = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + loggedUsername);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
            Optional<User> foundLogged = this.userRepository.findById(loggedUsername);
            if (foundLogged.isPresent()) {
                User userLogged = foundLogged.get();
                Optional<User> user = this.userRepository.findById(username);
                if (user.isPresent()) {
                    if (userLogged.getRole().getRoleName().equals(UserRole.ADMIN)) {
                        return ResponseEntity.ok().body(user.get());
                    } else {
                        if (userLogged.getUsername().equals(user.get().getUsername())) {
                            return ResponseEntity.ok().body(user.get());
                        } else {
                            ErrorDTO error = new ErrorDTO();
                            error.setError(ApplicationError.NO_PRIVILEGES);
                            error.setMessage("Logged user does not have privileges for this operation");
                            error.setDetailedMessage("Logged user does not have privileges for this operation");
                            return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                } else {
                    ErrorDTO error = new ErrorDTO();
                    error.setError(ApplicationError.USER_NOT_FOUND);
                    error.setMessage("User not found: " + username);
                    error.setDetailedMessage("User not found: " + username);
                    return new ResponseEntity<ErrorDTO>(error, HttpStatus.NOT_FOUND);
                }

            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Logged user not found: " + loggedUsername);
                error.setDetailedMessage("Logged user not found: " + loggedUsername);
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (UnsupportedJwtException | MalformedJwtException
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {

            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @PutMapping("/users/{username}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable String username,
            @RequestBody UserDTO userDto,
            @RequestHeader(value = "usertoken") String userToken) {

        try {
            String loggedUsername = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + loggedUsername);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
            Optional<User> found = this.userRepository.findById(username);
            Optional<User> foundLogged = this.userRepository.findById(loggedUsername);
            if (foundLogged.isPresent()) {
                User userLogged = foundLogged.get();
                if (found.isPresent()) {
                    User user = found.get();
                    if (userLogged.getRole().getRoleName().equals(UserRole.ADMIN)) {
                        if (userDto.getEmail() != null
                                && !userDto.getEmail().equals(user.getEmail())) {
                            user.setEmail(userDto.getEmail());
                        }
                        if (userDto.getName() != null
                                && !userDto.getName().equals(user.getName())) {
                            user.setName(userDto.getName());
                        }
                        if (userDto.getRole() != null
                                && userDto.getRole() != user.getRole().getRoleName()) {
                            user.setRole(new Role(userDto.getRole()));
                        }
                        PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
                        if (userDto.getPassword() != null
                                && userDto.getPassword().length() > 0
                                && !pe.matches(userDto.getPassword(), user.getPassword())) {
                            user.setPassword(pe.encode(userDto.getPassword()));
                        }
                        user.setEnabled(userDto.isEnabled());
                        this.userRepository.save(user);
                        return ResponseEntity.ok().body(user);
                    } else {
                        if (userLogged.getUsername().equals(user.getUsername())) {
                            if (userDto.getEmail() != null
                                    && !userDto.getEmail().equals(user.getEmail())) {
                                user.setEmail(userDto.getEmail());
                            }
                            if (userDto.getName() != null
                                    && !userDto.getName().equals(user.getName())) {
                                user.setName(userDto.getName());
                            }
                            if (userDto.getRole() != null
                                    && userDto.getRole() != user.getRole().getRoleName()) {
                                user.setRole(new Role(userDto.getRole()));
                            }
                            PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
                            if (userDto.getPassword() != null
                                    && userDto.getPassword().length() > 0
                                    && !pe.matches(userDto.getPassword(), user.getPassword())) {
                                user.setPassword(pe.encode(userDto.getPassword()));
                            }
                            user.setEnabled(userDto.isEnabled());
                            this.userRepository.save(user);
                            return ResponseEntity.ok().body(user);
                        } else {
                            ErrorDTO error = new ErrorDTO();
                            error.setError(ApplicationError.NO_PRIVILEGES);
                            error.setMessage("Logged user does not have privileges for this operation");
                            error.setDetailedMessage("Logged user does not have privileges for this operation");
                            return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }

                } else {
                    ErrorDTO error = new ErrorDTO();
                    error.setError(ApplicationError.USER_NOT_FOUND);
                    error.setMessage("User not found: " + username);
                    error.setDetailedMessage("User not found: " + username);
                    return new ResponseEntity<ErrorDTO>(error, HttpStatus.NOT_FOUND);
                }
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Logged user not found: " + loggedUsername);
                error.setDetailedMessage("Logged user not found: " + loggedUsername);
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (UnsupportedJwtException | MalformedJwtException
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {

            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
