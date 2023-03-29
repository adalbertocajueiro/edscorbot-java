package es.us.edscorbot.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String username;
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
