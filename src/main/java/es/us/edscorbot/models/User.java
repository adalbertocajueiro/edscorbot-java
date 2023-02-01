package es.us.edscorbot.models;

import es.us.edscorbot.util.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents an Ed Scorbot user.
 */
@Table(name="scorbot_users")
@Entity
@Setter
@Getter
public class User {
    /**
     * The email of the user. This attribute is also the identifer of the user
     */
    @Id
    @Column(name="email")
    private String email;

    /**
     * The complete name of the user.
     */
    @Column(name="name", nullable = false)
    private String name;

    /**
     * A flag that says if the user is enabled or not.
     */
    @Column(name="enabled", nullable = false)
    private boolean enabled;

    /**
     * The role of this user. 
     */
    @Column(name="role", nullable = false)
    private UserRole role;

    public User() {
    }
    public User(String email, String name, boolean enabled, UserRole role) {
        this.email = email;
        this.name = name;
        this.enabled = enabled;
        this.role = role;
    }
}
