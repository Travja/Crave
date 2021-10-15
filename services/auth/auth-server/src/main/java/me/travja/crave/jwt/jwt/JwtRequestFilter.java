package me.travja.crave.jwt.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import me.travja.crave.jwt.services.JWTDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JWTDetailsService jwtUserDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void setup() {
        String concatenated = secret;
        while (concatenated.getBytes().length < 32) {
            concatenated += concatenated;
        }
        JWTToken.STATIC_SECRET = new String(Base64.getEncoder().encode(concatenated.getBytes()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestTokenHeader = request.getHeader("Authorization");

        String   username = null;
        JWTToken jwt      = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            try {
                String jwtToken = requestTokenHeader.substring(7);
                jwt = JWTToken.parseToken(jwtToken);
                username = jwt.getUsername();
            } catch (IllegalArgumentException e) {
                logger.info("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.info("JWT Token has expired");
            } catch (SignatureException e) {
                logger.warn("JWT has been tampered with!");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);

            if (jwt.isValid(userDetails)) {
                UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                userAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userAuth);
            }
        }
        chain.doFilter(request, response);
    }

}