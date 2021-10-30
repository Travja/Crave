package me.travja.crave.common.repositories;

import me.travja.crave.common.models.auth.CraveUser;
import me.travja.crave.common.models.item.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<CraveUser, String> {

    Optional<CraveUser> findByUsernameIgnoreCase(String id);
    List<CraveUser> findByFavoritesContains(Item item);

}
