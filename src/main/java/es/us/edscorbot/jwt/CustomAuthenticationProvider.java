package es.us.edscorbot.jwt;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@Configurable
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Your code of custom Authentication

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        // to add more logic
        List<SimpleGrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("USER"));
        return new UsernamePasswordAuthenticationToken(username, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supports'");
    }
}
