package es.us.edscorbot.models;

import java.util.List;

import es.us.edscorbot.util.ListPointConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


/**
 * This class represens a complete ED Scorbot trajectory (a set of points) to be applied/sent
 * to the robot. An user can send many trajectories to the arm. These trajectories can be saved
 * for future purposes and reuse. 
 */
@Table(name="scorbot_trajectories")
@Entity
@Getter
@Setter
public class Trajectory {
    /**
     * The owner of this trajectory
     */
    @ManyToOne
    @JoinColumn(name="email", nullable=false)
    private User owner;

    /**
     * The points of this trajectory
     */
    @Convert(converter=ListPointConverter.class)
    @Column(name="points")
    private List<Point> points;

    /**
     * The timestap of this trajectory.
     */
    @Id
    @Column(name="timestamp")
    private long timestamp;

    
    
    /**
     * It creates an empty trajectory
     */
    public Trajectory() {
    }


    public Trajectory(User owner, long timestamp, List<Point> points) {
        this.owner = owner;
        this.timestamp = timestamp;
        this.points = points;
    }
}
