package me.travja.crave.jwt.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JWTDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private List<UserDetails> users = new ArrayList<>();

    @PostConstruct
    public void setupUsers() {
        users.add(new User("travja", passwordEncoder.encode("test"),
                new ArrayList<>()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetails> details = users.stream().filter(user -> user.getUsername().equals(username)).findFirst();

        if (details.isEmpty())
            throw new UsernameNotFoundException("User not found with username: " + username);

        return details.get();
    }

    public void addUser(UserDetails details) {
        users.add(details);
    }

}