package es.us.edscorbot.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.IUserRepository;

@RestController
public class UserController {
    
    @Autowired
    private IUserRepository userRepository;

    @GetMapping(value="/users", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllUsers(){
        try{
            return ResponseEntity.ok().body(this.userRepository.findAll());
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value="/users", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> save(@RequestBody User user){
        try{
            return ResponseEntity.ok().body(this.userRepository.save(user));
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value="/users/{email}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String email){
        
        try{
            Optional<User> user = this.userRepository.findById(email);
            if(user.isPresent()){
                return ResponseEntity.ok().body(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value="/users/{email}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateUser(@RequestBody User user){
        
        try{
            return ResponseEntity.ok().body(this.userRepository.save(user));
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value="/users/{email}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable String email){
        
        try{
            Optional<User> user = this.userRepository.findById(email);
            if(user.isPresent()){
                User realUser = user.get();
                realUser.setEnabled(false);
                this.userRepository.save(realUser);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
