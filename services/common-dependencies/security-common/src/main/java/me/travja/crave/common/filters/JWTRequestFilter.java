package me.travja.crave.common.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    private final LoadBalancerClient loadBalancerClient;
    private final RestTemplate       restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && !authHeader.trim().isEmpty() && authHeader.startsWith("Bearer ")) {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0");
            headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
            headers.add(HttpHeaders.AUTHORIZATION, authHeader);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ServiceInstance authServer = loadBalancerClient.choose("auth-service");
            if (authServer == null) { // Can't authenticate... just go through I guess
                System.err.println("Couldn't find Auth Service!");
                chain.doFilter(request, response);
                return;
            }

            ResponseEntity<AuthResponse> res = restTemplate.exchange(authServer.getUri() + "/checkauth",
                    HttpMethod.POST,
                    entity, AuthResponse.class);

            AuthResponse auth = res.getBody();
            if (res.getStatusCode() == HttpStatus.OK && auth != null && auth.isValid() && SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthToken userAuth = new AuthToken(auth.getUsername(), null, auth.getRoles());
                userAuth.setFavorites(auth.getFavorites());
                userAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(userAuth);
                logger.info("User authenticated.");
            }
        }

        chain.doFilter(request, response);
    }

}