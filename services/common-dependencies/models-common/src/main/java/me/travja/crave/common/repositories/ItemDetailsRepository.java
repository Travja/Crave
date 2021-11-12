package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.ItemDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemDetailsRepository extends CrudRepository<ItemDetails, Long> {

    List<ItemDetails> findAllByItemUpcUpc(String upc);
    Optional<ItemDetails> findByItemUpcUpcAndStoreId(String upc, long storeId);
    Optional<ItemDetails> findFirstByItemNameLike(String name);
    Optional<ItemDetails> findFirstByItemNameLikeOrderBySalesNewPriceAscPriceAsc(String name);

}
