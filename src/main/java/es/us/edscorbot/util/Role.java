package es.us.edscorbot.util;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scorbot_roles")
public class Role {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole roleName;

    public Role() {

    }

    public Role(UserRole name) {
        this.roleName = name;
    }
}