package es.us.edscorbot.util;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse implements Serializable {

    private String username;
    private String email;
    private String name;
    private boolean enabled;
    private UserRole role;
    private String token;

    // need default constructor for JSON Parsing
    public LoginResponse() {

    }

}