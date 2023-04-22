package es.us.edscorbot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import es.us.edscorbot.jwt.JwtTokenUtil;
import es.us.edscorbot.models.Trajectory;
import es.us.edscorbot.models.User;
import es.us.edscorbot.repositories.ITrajectoryRepository;
import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.ApplicationError;
import es.us.edscorbot.util.ErrorDTO;
import es.us.edscorbot.util.TrajectoryDTO;
import es.us.edscorbot.util.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TrajectoryController {
    
    @Autowired
    private ITrajectoryRepository trajectoryRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getAllTrajectories(@RequestHeader(value = "usertoken") String userToken){
        try{
            String username = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + username);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
            List<Trajectory> trajectories = this.trajectoryRepository.findAll();
            Optional<User> user = this.userRepository.findById(username);
            if (user.isPresent()) {
                User loggedUser = user.get();
                if (loggedUser.getRole().getRoleName() == UserRole.USER) {
                    trajectories.removeIf(t -> !t.getOwner().getUsername().equals(username));
                }
                return ResponseEntity.ok().body(trajectories);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage("Trajectory owner not found: " + username);
                error.setDetailedMessage("Trajectory owner not found: " + username);
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.NOT_FOUND);
            }
            
        } catch (UnsupportedJwtException | MalformedJwtException
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {

            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e){
            
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } 
    }

    @PostMapping(value="/trajectories", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> save(@RequestBody TrajectoryDTO trajectoryDTO, 
            @RequestHeader(value = "usertoken") String userToken){
        try{
            String username = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + username);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
            Trajectory trajectory = new Trajectory();
            Optional<User> user = this.userRepository.findById(username);
            if (user.isPresent()) {
                trajectory.setOwner(user.get());
                trajectory.setTimestamp(System.currentTimeMillis());
                trajectory.setPoints(trajectoryDTO.getPoints());
                return ResponseEntity.ok().body(this.trajectoryRepository.save(trajectory));
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage("Trajectory owner not found: " + username);
                error.setDetailedMessage("Trajectory owner not found: " + username);
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.NOT_FOUND);
            }
        } catch (UnsupportedJwtException | MalformedJwtException
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {

            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @DeleteMapping(value="/trajectories/{timestamp}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable long timestamp, 
            @RequestHeader(value = "usertoken") String userToken){
        try{
            String username = this.jwtTokenUtil.getUsernameFromToken(userToken);
            if (this.jwtTokenUtil.isTokenExpired(userToken)) {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.USER_NOT_FOUND);
                error.setMessage("Token expired for user: " + username);
                error.setDetailedMessage("JWT token expired");
                return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);
            }
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
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage("Trajectory owner not found");
                error.setDetailedMessage("Trajectory owner not found");
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);            }
        } catch (UnsupportedJwtException | MalformedJwtException
                | SignatureException | ExpiredJwtException | IllegalArgumentException e) {

            ErrorDTO error = new ErrorDTO();
            error.setError(ApplicationError.USER_NOT_FOUND);
            error.setMessage("Invalid token");
            error.setDetailedMessage(e.getMessage());
            return new ResponseEntity<ErrorDTO>(error, HttpStatus.UNAUTHORIZED);

        } catch (Exception e){
            if (e instanceof AuthenticationException) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            } else {
                ErrorDTO error = new ErrorDTO();
                error.setError(ApplicationError.INTERNAL_ERROR);
                error.setMessage(e.getMessage());
                error.setDetailedMessage(e.getMessage());
                return new ResponseEntity<String>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
    }

}
