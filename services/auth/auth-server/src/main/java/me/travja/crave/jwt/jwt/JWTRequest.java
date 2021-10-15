package me.travja.crave.jwt.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class JWTRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    @Getter
    @Setter
    private String username,
            password;

}