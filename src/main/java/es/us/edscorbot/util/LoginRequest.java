package es.us.edscorbot.util;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String username;
    private String password;

    // need default constructor for JSON Parsing
    public LoginRequest() {

    }

    public LoginRequest(String username, String password) {
        this.setUsername(username);
        this.setPassword(password);
    }

}