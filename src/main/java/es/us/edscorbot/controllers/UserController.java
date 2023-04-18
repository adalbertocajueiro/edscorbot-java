package es.us.edscorbot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.jwt.AuthenticationException;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.GlobalPasswordEncoder;
import es.us.edscorbot.util.Role;
import es.us.edscorbot.util.UserDTO;
import es.us.edscorbot.util.UserRole;

@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private IUserRepository userRepository;

    @GetMapping(value="/users", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "username") String username){
        try{
            Optional<User> found = this.userRepository.findById(username);
            if (found.isPresent()) {
                User user = found.get();
                List<User> users = this.userRepository.findAll();
                if(user.getRole().getRoleName().equals(UserRole.USER)){
                    users.removeIf(u -> !u.getUsername().equals(username));
                } 
                return ResponseEntity.ok().body(users);
            } else {
                return new ResponseEntity<String>("Trajectory owner not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e){
            if(e instanceof AuthenticationException){
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else{
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        }
    }

    @GetMapping(value="/users/{username}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String username,
            @RequestHeader(value = "username") String loggedUsername){
        
        try{
            Optional<User> foundLogged = this.userRepository.findById(loggedUsername);
            if (foundLogged.isPresent()) {
                User userLogged = foundLogged.get();
                Optional<User> user = this.userRepository.findById(username);
                if(userLogged.getRole().getRoleName().equals(UserRole.ADMIN)){
                    if (user.isPresent()) {
                        return ResponseEntity.ok().body(user.get());
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } else {
                    if (userLogged.getUsername().equals(user.get().getUsername())){
                        return ResponseEntity.ok().body(user.get());
                    } else{
                        return new ResponseEntity<String>("Non admin users cannot get information about other users",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                return new ResponseEntity<String>("Logged user not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            
        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @PutMapping("/users/{username}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable String username, 
            @RequestBody UserDTO userDto){

        try{
            Optional<User> found = this.userRepository.findById(username);
            if (found.isPresent()) {
                User user = found.get();
                if(userDto.getEmail() != null 
                    && !userDto.getEmail().equals(user.getEmail())){
                    user.setEmail(userDto.getEmail());
                }
                if(userDto.getName() != null 
                    && !userDto.getName().equals(user.getName())){
                    user.setName(userDto.getName());
                }
                if(userDto.getRole() != null
                    && userDto.getRole() != user.getRole().getRoleName()){
                    user.setRole(new Role(userDto.getRole()));
                }
                PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
                if(userDto.getPassword() != null 
                    && userDto.getPassword().length() > 0
                    && !pe.matches(userDto.getPassword(), user.getPassword())){
                    user.setPassword(pe.encode(userDto.getPassword()));
                }
                user.setEnabled(userDto.isEnabled());
                this.userRepository.save(user);
                return ResponseEntity.ok().body(user);
            } else {
                return new ResponseEntity<String>("User not found!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @DeleteMapping(value="/users/{username}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> deleteUser(
            @PathVariable String username,
            @RequestHeader(value = "username") String loggedUsername){
        
        try{
            Optional<User> foundLogged = this.userRepository.findById(loggedUsername);
            if (foundLogged.isPresent()) {
                User userLogged = foundLogged.get();
                Optional<User> user = this.userRepository.findById(username);
                if(userLogged.getRole().getRoleName().equals(UserRole.ADMIN)){
                    if (user.isPresent()) {
                        User realUser = user.get();
                        realUser.setEnabled(false);
                        this.userRepository.save(realUser);
                        return ResponseEntity.ok().body(realUser);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } else {
                    if (userLogged.getUsername().equals(user.get().getUsername())){
                        User realUser = user.get();
                        realUser.setEnabled(false);
                        this.userRepository.save(realUser);
                        return ResponseEntity.ok().body(realUser);
                    } else{
                        return new ResponseEntity<String>("Non admin users cannot get information about other users",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                return new ResponseEntity<String>("Logged user not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
