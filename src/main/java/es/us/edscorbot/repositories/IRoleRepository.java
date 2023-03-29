package es.us.edscorbot.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import es.us.edscorbot.util.Role;


public interface IRoleRepository extends JpaRepository<Role,String> {
    
}
