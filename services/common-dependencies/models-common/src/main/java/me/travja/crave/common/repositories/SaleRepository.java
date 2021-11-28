package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.sale.Sale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAll();
    List<Sale> findAllByStoreId(long id);
    List<Sale> findAllByStoreName(String name);
    List<Sale> findAllByItemItemUpcUpc(String upc);

    @Query("from Sale s where s.item.item.name like %:query% or s.store.name like %:query% " +
            "or s.item.item.upc.upc like %:query%")
    List<Sale> findAllByQuery(String query, Pageable pageable);

    List<Sale> findAllByItem(ItemDetails item);

}
