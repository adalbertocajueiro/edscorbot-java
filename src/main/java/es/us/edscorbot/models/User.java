package es.us.edscorbot.models;

import es.us.edscorbot.util.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * This class represents an Ed Scorbot user.
 */
@Entity
public class User {
    /**
     * The email of the user. This attribute is also the identifer of the user
     */
    @Id
    private String email;

    /**
     * The complete name of the user.
     */
    @Column(nullable = false)
    private String name;

    /**
     * A flag that says if the user is enabled or not.
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * The role of this user. 
     */
    @Column(nullable = false)
    private UserRole role;

    public User() {
    }
    public User(String email, String name, boolean enabled, UserRole role) {
        this.email = email;
        this.name = name;
        this.enabled = enabled;
        this.role = role;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    
    
}
