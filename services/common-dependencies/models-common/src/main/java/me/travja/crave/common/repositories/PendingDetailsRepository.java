package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.PendingDetails;
import me.travja.crave.common.models.store.Store;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingDetailsRepository extends CrudRepository<PendingDetails, Long> {

    List<PendingDetails> findAllByItemUpcUpc(String upc);
    Optional<PendingDetails> findByItemUpcUpcAndStoreId(String upc, long storeId);
    Optional<PendingDetails> findFirstByItemNameLike(String name);
    Optional<PendingDetails> findFirstByItemNameLikeOrderBySalesNewPriceAscPriceAsc(String name);
    Optional<PendingDetails> findFirstByItemNameLikeAndStoreOrderBySalesNewPriceAscPriceAsc(String name, Store store);
    List<PendingDetails> findAllByItemNameLike(String name);

}
