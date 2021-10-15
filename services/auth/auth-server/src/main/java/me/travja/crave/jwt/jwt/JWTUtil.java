package me.travja.crave.jwt.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    static boolean isExpirationIgnored(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    public static String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
//        claims.put("test", new ArrayList<>(List.of(new String[]{"Hey... does this work?", "Indeed it does!"})));
        return generateToken(userDetails.getUsername(), claims);
    }

    private static String generateToken(String subject, Map<String, Object> claims) {
        return JWTToken.from(subject, claims).getToken();
    }

}