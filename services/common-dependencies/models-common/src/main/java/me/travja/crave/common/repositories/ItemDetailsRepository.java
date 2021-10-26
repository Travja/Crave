package me.travja.crave.common.repositories;

import me.travja.crave.common.models.ItemDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemDetailsRepository extends CrudRepository<ItemDetails, Long> {

    List<ItemDetails> findAllByItemUpcUpc(String upc);

}
