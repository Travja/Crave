package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByUpcUpc(String upc);

    List<Item> findAllByOrderByNameAsc();

    @Query("from Item i where i.name like %:query% or i.description like %:query% " +
            "or i.upc.upc like %:query% order by i.name asc")
    List<Item> findAllByQuery(String query);

    @Query("from Item i where i.name like %:name%")
    List<Item> findAllByNameLike(String name, Pageable pageable);

}
