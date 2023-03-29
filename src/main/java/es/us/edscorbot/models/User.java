package es.us.edscorbot.models;

import es.us.edscorbot.util.Role;
import es.us.edscorbot.util.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    
    @Id
    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(name="email")
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    @Size(max = 120)
    private String name;

    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "roleName", nullable = false)
    private Role role;

    public User() {
    }

    public User(String username, String email, String password, String name) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.enabled = true;
        this.role = new Role(UserRole.USER);
    }

    public User(String username, String email, String password, String name, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.enabled = true;
        this.role = role;
    }
}
