package me.travja.crave.common.repositories;

import me.travja.crave.common.models.CraveUser;
import me.travja.crave.common.models.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<CraveUser, String> {

    Optional<CraveUser> findByUsernameIgnoreCase(String id);
    List<CraveUser> findByFavoritesContains(Item item);

}
