package me.travja.crave.common.filters;

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
    private boolean      valid;
    private List<String> roles     = new ArrayList<>();
    private List<String> favorites = new ArrayList<>();
    private String       message   = "";

    public AuthResponse(String username, List<String> roles, boolean valid) {
        this.username = username;
        this.roles = roles;
        this.valid = valid;
    }

}
