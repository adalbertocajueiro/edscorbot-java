package es.us.edscorbot.models;

import java.util.LinkedList;

/**
 * This class represens a complete ED Scorbot trajectory (a set of points) to be applied/sent
 * to the robot. An user can send many trajectories to the arm. These trajectories can be saved
 * for future purposes and reuse. 
 */
public class Trajectory {
    /**
     * The owner of this trajectory
     */
    private User owner;

    /**
     * The timestap of this trajectory.
     */
    private long timestamp;

    /**
     * The points of this trajectory
     */
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


    public User getOwner() {
        return owner;
    }


    public void setOwner(User owner) {
        this.owner = owner;
    }


    public long getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public LinkedList<Point> getPoints() {
        return points;
    }


    public void setPoints(LinkedList<Point> points) {
        this.points = points;
    }

    
}
