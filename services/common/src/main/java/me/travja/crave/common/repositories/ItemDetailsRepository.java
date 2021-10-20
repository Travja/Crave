package me.travja.crave.common.repositories;

import me.travja.crave.common.models.ItemDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemDetailsRepository extends CrudRepository<ItemDetails, Long> {

    Optional<ItemDetails> findByItemUpc(String upc);

}
