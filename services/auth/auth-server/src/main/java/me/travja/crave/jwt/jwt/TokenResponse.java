package me.travja.crave.jwt.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TokenResponse {

    @Getter
    private final String token;

}