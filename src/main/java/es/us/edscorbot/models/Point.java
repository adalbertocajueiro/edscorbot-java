package es.us.edscorbot.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * This class representas an ED Scorbot point, that is, a tuple containing 
 * for values (one for each joint). The possible values for each joint should
 * respect the ranges of values allowed for each joint individually.
 */
@Embeddable
@Getter
@Setter
public class Point implements Serializable{
    /**
     * The ref value for Joint 1
     */
    @Column(name="j1Ref")
    private double j1Ref;

    /**
     * The ref value for Joint 2
     */
    @Column(name="j2Ref")
    private double j2Ref;

    /**
     * The ref value for Joint 3
     */
    @Column(name="j3Ref")
    private double j3Ref;

    /**
     * The ref value for Joint 4
     */
    @Column(name="j4Ref")
    private double j4Ref;

    /**
     * This constructor creates one point with ref values 0 for all joints. This corresponds 
     * to the home position 
     */
    public Point() {
    }

     /**
     * This constructor creates one point with specific ref values for each joint.
     */
    public Point(double j1Ref, double j2Ref, double j3Ref, double j4Ref) {
        this.j1Ref = j1Ref;
        this.j2Ref = j2Ref;
        this.j3Ref = j3Ref;
        this.j4Ref = j4Ref;
    }
}
