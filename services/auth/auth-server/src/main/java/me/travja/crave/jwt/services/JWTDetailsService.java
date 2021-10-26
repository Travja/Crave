package me.travja.crave.jwt.services;

import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.CraveUser;
import me.travja.crave.common.repositories.UserRepo;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JWTDetailsService implements UserDetailsService {

    private final UserRepo        userRepo;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void setupUsers() {
        if (!containsUser("travja"))
            userRepo.save(new CraveUser("travja", "tjeggett@yahoo.com",
                    passwordEncoder.encode("test"), Set.of(), List.of("ADMIN", "USER"), true));
    }

    @Override
    public CraveUser loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CraveUser> details = userRepo.findByUsernameIgnoreCase(username);

        if (details.isEmpty())
            throw new UsernameNotFoundException("User not found with username: " + username);

        return details.get();
    }

    public void addUser(CraveUser details) throws UserExistsException {
        if (!containsUser(details.getUsername()))
            userRepo.save(details);
        else
            throw new UserExistsException();
    }

    public boolean containsUser(String username) {
        return userRepo.findByUsernameIgnoreCase(username).isPresent();
    }

    public class UserExistsException extends Exception {}

}