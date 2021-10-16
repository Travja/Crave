package me.travja.crave.jwt.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class JWTRequest {

    @Getter
    @Setter
    private String username,
            password;

}