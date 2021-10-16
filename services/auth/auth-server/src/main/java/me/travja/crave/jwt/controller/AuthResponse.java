package me.travja.crave.jwt.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    @Getter
    private String       username;
    @Getter
    private List<String> roles = new ArrayList<>();
    @Getter
    private boolean      valid;
    @Getter
    private String       message = "";

    public AuthResponse(String username, List<String> roles, boolean valid) {
        this.username = username;
        this.roles = roles;
        this.valid = valid;
    }

}
