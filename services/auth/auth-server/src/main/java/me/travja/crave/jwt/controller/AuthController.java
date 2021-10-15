package me.travja.crave.jwt.controller;

import lombok.AllArgsConstructor;
import me.travja.crave.jwt.jwt.*;
import me.travja.crave.jwt.services.JWTDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Objects;

@RestController
@CrossOrigin
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTDetailsService     jwtDetailsService;
    private final PasswordEncoder       passwordEncoder;

    @PostMapping("/checkauth")
    public void checkAuth(@RequestHeader("Authorization") String header) throws JWTAuthException {
        if (header == null || !header.startsWith("Bearer "))
            throw new JWTAuthException();

        boolean valid;
        String token = header.substring(7);
        try {
            JWTToken jwt = JWTToken.parseToken(token);
            UserDetails user = jwtDetailsService
                    .loadUserByUsername(jwt.getUsername());
            valid = jwt.isValid(user);
        } catch (Exception e) {
            valid = false;
        }

        if (!valid) { //TODO Make a proper error response.
            throw new JWTAuthException();
        }
    }

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> generateAuthenticationToken(@RequestBody JWTRequest authRequest) throws Exception {
        authenticate(authRequest.getUsername(), authRequest.getPassword());

        UserDetails userDetails = jwtDetailsService
                .loadUserByUsername(authRequest.getUsername());

        String token = JWTUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestParam String username, @RequestParam String password) {
        UserDetails details = new User(username, passwordEncoder.encode(password), new ArrayList<>());
        jwtDetailsService.addUser(details);

        return ResponseEntity.ok(new JwtResponse(JWTUtil.generateToken(details)));
    }

    private void authenticate(String username, String password) throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}