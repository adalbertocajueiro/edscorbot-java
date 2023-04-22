package es.us.edscorbot.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.us.edscorbot.util.GlobalPasswordEncoder;

@Service
public class Authenticator {
    @Autowired
    private CustomAuthenticationProvider authenticationManager;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public String authenticate(String username, String password) throws DisabledException, BadCredentialsException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        PasswordEncoder pe = GlobalPasswordEncoder.getGlobalEncoder();
        if (!pe.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid user/password");
        }

        return jwtTokenUtil.generateToken(userDetails);
    }
}
