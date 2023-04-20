package es.us.edscorbot.repositories;




import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.us.edscorbot.models.User;
import es.us.edscorbot.util.UserBuilder;
import jakarta.annotation.PostConstruct;

@Component
public class InitialData {
    private static final Logger LOG = Logger.getLogger(InitialData.class.getName());

    @Autowired
    private IUserRepository userRepository;

    @PostConstruct
    public void init() {
        
        User user = UserBuilder.rootUser();
        Optional<User> found = this.userRepository.findById(user.getUsername());
        if(!found.isPresent()){
            this.userRepository.save(user);
            LOG.info("Root user added to the database");
        }
    }
}
