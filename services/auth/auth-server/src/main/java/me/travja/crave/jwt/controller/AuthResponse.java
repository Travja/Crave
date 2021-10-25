package me.travja.crave.jwt.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String       username;
    private List<String> roles     = new ArrayList<>();
    private boolean      valid;
    private String       message   = "";
    private List<String> favorites = new ArrayList<>();

    public AuthResponse(String username, List<String> roles, boolean valid) {
        this.username = username;
        this.roles = roles;
        this.valid = valid;
    }

    public AuthResponse(String username, List<String> roles, boolean valid, String message) {
        this(username, roles, valid);
        this.message = message;
    }

    public AuthResponse setFavorites(List<String> favorites) {
        this.favorites = favorites;
        return this;
    }

}
