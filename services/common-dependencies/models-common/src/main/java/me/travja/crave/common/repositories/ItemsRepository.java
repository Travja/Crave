package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ItemDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByUpcUpc(String upc);


    @Query("from Item i where i.name like %:query% or i.description like %:query% " +
            "or i.upc.upc like %:query% order by i.name asc")
    List<Item> findAllByQuery(String query);

    Page<Item> findAll(Pageable pageable);
    Page<Item> findAllByNameLike(String name, Pageable pageable);

    @EntityGraph(attributePaths = "details") //Why this actually makes it so we can sort... I don't know.
    Page<Item> findAllByNameLikeAndDetailsStoreId(String name, long storeId, Pageable pageable);
    @EntityGraph(attributePaths = "details")
    Page<Item> findAllByNameLikeAndDetailsStoreNameLike(String name, String storeName, Pageable pageable);


}
