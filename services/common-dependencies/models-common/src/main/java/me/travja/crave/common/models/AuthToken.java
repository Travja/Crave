package me.travja.crave.common.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AuthToken extends UsernamePasswordAuthenticationToken {

    private List<String> favorites = new ArrayList<>();

    public AuthToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public AuthToken(Object principal, Object credentials, Collection<String> authorities) {
        super(principal, credentials, authorities.stream().map(auth -> new SimpleGrantedAuthority(auth)).collect(Collectors.toList()));
    }
}
