package es.us.edscorbot.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;
    private String name;
    private boolean enabled;
    private UserRole role;
}
