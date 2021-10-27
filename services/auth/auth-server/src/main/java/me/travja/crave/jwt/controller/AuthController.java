package me.travja.crave.jwt.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.travja.crave.common.models.CraveUser;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.jwt.jwt.*;
import me.travja.crave.common.models.AuthToken;
import me.travja.crave.common.models.ListItem;
import me.travja.crave.jwt.services.JWTDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
//@CrossOrigin
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTDetailsService     jwtDetailsService;
    private final PasswordEncoder       passwordEncoder;
    private final Logger                logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/checkauth")
    public ResponseEntity<AuthResponse> checkAuth(
            @RequestHeader("Authorization") String header) throws JWTAuthException {
        if (header == null || !header.startsWith("Bearer "))
            throw new JWTAuthException();

        boolean                      valid;
        ResponseEntity<AuthResponse> resp  = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        String                       token = header.substring(7);
        try {
            JWTToken jwt = JWTToken.parseToken(token);
            CraveUser user = jwtDetailsService
                    .loadUserByUsername(jwt.getUsername());
            valid = jwt.isValid(user);
            if (valid) {
                resp = ResponseEntity.ok(new AuthResponse(user.getUsername(),
                        user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()),
                        true)
                        .setFavorites(
                                user.getFavorites().stream()
                                        .map(itm -> itm.getUpc().getUPC())
                                        .collect(Collectors.toList())
                        ));
            }
        } catch (Exception e) {
            valid = false;
        }

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("UNAUTHORIZED", Collections.emptyList(), false, "User not authenticated."));
        }

        return resp;
    }

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> generateAuthenticationToken(@RequestBody JWTRequest authRequest) throws Exception {
        authenticate(authRequest.getUsername(), authRequest.getPassword());

        CraveUser userDetails = jwtDetailsService
                .loadUserByUsername(authRequest.getUsername());

        String token = JWTUtil.generateToken(userDetails);

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password) throws JWTDetailsService.UserExistsException {
        CraveUser details = new CraveUser(username, email, passwordEncoder.encode(password), List.of("USER"));
        jwtDetailsService.addUser(details);

        return ResponseEntity.ok(new TokenResponse(JWTUtil.generateToken(details)));
    }

    @GetMapping("/checkuser/{user}")
    public ResponseEntity checkUser(@PathVariable String user) {
        return ResponseEntity.ok(new UserAvailable(!jwtDetailsService.containsUser(user)));
    }


    @PostMapping("/list")
    public ResponseObject saveList(@RequestBody List<ListItem> list, Authentication auth) {
        if (auth instanceof AuthToken) {
            CraveUser user = jwtDetailsService.loadUserByUsername(auth.getName());

            user.setShoppingList(list);

            jwtDetailsService.saveUser(user);
            return ResponseObject.success();
        }

        return ResponseObject.failure();
    }

    @GetMapping("/list")
    public List<ListItem> getList(Authentication auth) {
        if (auth instanceof AuthToken) {
            CraveUser user = jwtDetailsService.loadUserByUsername(auth.getName());

            return user.getShoppingList();
        }

        return Collections.emptyList();
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

    @Data
    @AllArgsConstructor
    private class UserAvailable {
        private boolean available;
    }

}