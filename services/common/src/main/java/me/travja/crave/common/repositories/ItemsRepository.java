package me.travja.crave.common.repositories;

import me.travja.crave.common.models.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Long> {

    Optional<Item> findByUpcUpc(String upc);

}
