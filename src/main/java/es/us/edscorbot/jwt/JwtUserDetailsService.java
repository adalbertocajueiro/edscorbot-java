package es.us.edscorbot.jwt;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.us.edscorbot.repositories.IUserRepository;
import es.us.edscorbot.util.UserBuilder;
import es.us.edscorbot.util.UserRole;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        User result = null; 

        es.us.edscorbot.models.User root = UserBuilder.rootUser();
        if(root.getUsername().equalsIgnoreCase(username)){ //root user

            result = new User(root.getUsername(), root.getPassword(), new ArrayList<GrantedAuthority>());
             
        } else { //get the user from the database
            Optional<es.us.edscorbot.models.User> userOpt = this.userRepository.findById(username);
            if(userOpt.isPresent()){
                es.us.edscorbot.models.User user = userOpt.get();
                result = new User(user.getUsername(), user.getPassword(),new ArrayList<>());
            } else {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }
            
        }
        return result;
    }
}