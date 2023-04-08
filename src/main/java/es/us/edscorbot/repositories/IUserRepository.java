package es.us.edscorbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.us.edscorbot.models.User;


public interface IUserRepository extends JpaRepository<User,String> {
    
}
