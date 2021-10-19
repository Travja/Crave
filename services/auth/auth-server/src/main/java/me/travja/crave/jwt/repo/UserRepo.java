package me.travja.crave.jwt.repo;

import me.travja.crave.jwt.services.AuthUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<AuthUser, String> {

    Optional<AuthUser> findByUsernameIgnoreCase(String id);

}
