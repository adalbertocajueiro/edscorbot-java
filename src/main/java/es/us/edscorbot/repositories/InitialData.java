package es.us.edscorbot.repositories;

import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.us.edscorbot.models.User;
import es.us.edscorbot.util.Role;
import es.us.edscorbot.util.UserBuilder;
import es.us.edscorbot.util.UserRole;
import jakarta.annotation.PostConstruct;

@Component
public class InitialData {
    private static final Logger LOG = Logger.getLogger(InitialData.class.getName());

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @PostConstruct
    public void init() {
        
        Role adminRole = new Role(UserRole.ADMIN);
        Role userRole = new Role(UserRole.USER);

        this.roleRepository.save(adminRole);
        this.roleRepository.save(userRole);

        User user = UserBuilder.rootUser();
        Optional<User> found = this.userRepository.findById(user.getUsername());
        if(!found.isPresent()){
            this.userRepository.save(user);
            LOG.info("Root user added to the database");
        }
    }
}
