package es.us.edscorbot.util;

import org.springframework.security.crypto.password.PasswordEncoder;

import es.us.edscorbot.models.User;

public class UserBuilder {
    public static User rootUser(){
        User root = new User();
        root.setUsername("root");
        PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
        root.setPassword(pe.encode("edscorbot"));
        root.setEmail("adalberto@computacao.ufcg.edu.br");
        root.setEnabled(true);
        root.setName("Adalberto Cajueiro de Farias");
        root.setRole(new Role(UserRole.ADMIN));
        
        return root;
    }
}
