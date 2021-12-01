package me.travja.crave.common.repositories;

import me.travja.crave.common.models.sale.BatchSale;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BatchRepository extends CrudRepository<BatchSale, Long> {

    List<BatchSale> findAllByApprovedFalse();

}
