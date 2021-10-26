package me.travja.crave.common.repositories;

import me.travja.crave.common.models.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Long> {

    Optional<Item> findByUpcUpc(String upc);

    @Query("from Item i where i.name like %:query% or i.description like %:query% " +
            "or i.upc.upc like %:query%")
    List<Item> findAllByQuery(String query);

}
