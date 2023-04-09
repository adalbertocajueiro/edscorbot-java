package es.us.edscorbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import es.us.edscorbot.models.Trajectory;


public interface ITrajectoryRepository extends JpaRepository<Trajectory,Long> {
    
}
