package es.us.edscorbot.models;

import java.util.LinkedList;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import es.us.edscorbot.util.ListPointConverter;

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
    @Column(name="owner",nullable=false)
    @JoinColumn(name="email", nullable=false)
    private User owner;

    /**
     * The timestap of this trajectory.
     */
    @Id
    @Column(name="timestamp")
    private long timestamp;

    /**
     * The points of this trajectory
     */
    @Convert(converter = ListPointConverter.class)
    private LinkedList<Point> points;
    
    /**
     * It creates an empty trajectory
     */
    public Trajectory() {
    }


    public Trajectory(User owner, long timestamp, LinkedList<Point> points) {
        this.owner = owner;
        this.timestamp = timestamp;
        this.points = points;
    }
}
