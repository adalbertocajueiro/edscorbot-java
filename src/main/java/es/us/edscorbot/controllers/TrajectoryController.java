package es.us.edscorbot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.jwt.AuthenticationException;
import es.us.edscorbot.models.Trajectory;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.ITrajectoryRepository;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.TrajectoryDTO;
import es.us.edscorbot.util.UserRole;


@RestController
@RequestMapping("/api")
public class TrajectoryController {
    
    @Autowired
    private ITrajectoryRepository trajectoryRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllTrajectories(@RequestHeader(value = "username") String username){
        try{
            List<Trajectory> trajectories = this.trajectoryRepository.findAll();
            if(!username.equals("root")){
                Optional<User> user = this.userRepository.findById(username);
                if (user.isPresent()) {
                    User loggedUser = user.get();
                    if (loggedUser.getRole().getRoleName() == UserRole.USER) {
                        trajectories.removeIf(t -> !t.getOwner().getUsername().equals(username));
                    }
                    return ResponseEntity.ok().body(trajectories);
                } else {
                    return new ResponseEntity<String>("Trajectory owner not found", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return ResponseEntity.ok().body(trajectories);
            }
            
            
        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @PostMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> save(@RequestBody TrajectoryDTO trajectoryDTO, 
            @RequestHeader(value = "username") String username){
        try{
            Trajectory trajectory = new Trajectory();
            Optional<User> user = this.userRepository.findById(username);
            if (user.isPresent()) {
                trajectory.setOwner(user.get());
                trajectory.setTimestamp(System.currentTimeMillis());
                trajectory.setPoints(trajectoryDTO.getPoints());
                return ResponseEntity.ok().body(this.trajectoryRepository.save(trajectory));
            } else {
                return new ResponseEntity<String>("Trajectory owner not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @DeleteMapping(value="/trajectories/{timestamp}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable long timestamp, 
            @RequestHeader(value = "username") String username){
        try{
            Optional<User> user = this.userRepository.findById(username);
            if (user.isPresent()) {
                User loggedUser = user.get();
                Optional<Trajectory> traj = this.trajectoryRepository.findById(timestamp);
                if(traj.isPresent()){
                    Trajectory foundTraj = traj.get();
                    if(loggedUser.getRole().getRoleName() == UserRole.ADMIN){
                        this.trajectoryRepository.deleteById(timestamp);
                    } else {
                        if (foundTraj.getOwner().getUsername().equals(username)) {
                            this.trajectoryRepository.deleteById(timestamp);
                        }
                    }
                    
                }
                return ResponseEntity.ok().body(true);
            } else {
                return new ResponseEntity<String>("Trajectory owner not found", HttpStatus.INTERNAL_SERVER_ERROR);
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
