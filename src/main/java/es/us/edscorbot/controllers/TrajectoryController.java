package es.us.edscorbot.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.us.edscorbot.models.Trajectory;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.ITrajectoryRepository;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.TrajectoryDTO;

@RestController
public class TrajectoryController {
    
    @Autowired
    private ITrajectoryRepository trajectoryRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllTrajectories(){
        try{
            return ResponseEntity.ok().body(this.trajectoryRepository.findAll());
        } catch (Exception e){
            return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> save(@RequestBody TrajectoryDTO trajectoryDTO){
        Trajectory trajectory = new Trajectory();
        Optional<User> user = this.userRepository.findById(trajectoryDTO.getEmail());
        if(user.isPresent()){
            trajectory.setOwner(user.get());
            trajectory.setTimestamp(System.currentTimeMillis());
            trajectory.setPoints(trajectoryDTO.getPoints());
            try{
                return ResponseEntity.ok().body(this.trajectoryRepository.save(trajectory));
            } catch (Exception e){
                return new ResponseEntity<String>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<String>("Trajectory owner not found",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
