package me.travja.crave.jwt.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.auth.CraveUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JWTToken {

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    static String STATIC_SECRET;

    @Getter
    private final String token;
    @Getter
    private final String username;

    @Getter
    private final Date issuedAt, expiration;
    private final Claims claims;


    public static JWTToken parseToken(String token) {
        Claims claims = JWTToken.getAllClaims(token);
        JWTToken jwt = new JWTToken(token, claims.getSubject(), claims.getIssuedAt(), claims.getExpiration(),
                claims);
        return jwt;
    }

    public static Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(STATIC_SECRET.getBytes())).build()
                .parseClaimsJws(token).getBody();
    }

    public static JWTToken from(String username, Map<String, Object> claims) {
        String token = build(username, claims);
        return parseToken(token);
    }

    private static String build(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .signWith(Keys.hmacShaKeyFor(STATIC_SECRET.getBytes()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .compact();
    }

    public boolean isExpired() {
        return getExpiration().before(new Date());
    }

    public boolean canRefresh() {
        return (!isExpired() || JWTUtil.isExpirationIgnored(token));
    }

    public List<String> getAuthorities() {
        return get("authorities");
    }

    public String getString(String key) {
        return claims.get(key, String.class);
    }

    public double getDouble(String key) {
        return claims.get(key, Double.class);
    }

    public <T> T get(String key) {
        return (T) claims.get(key, Object.class);
    }

    public boolean isValid(CraveUser userDetails) {
        return getUsername().equals(userDetails.getUsername()) && !isExpired();
    }

}
