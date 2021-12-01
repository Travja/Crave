package me.travja.crave.common.repositories;

import me.travja.crave.common.models.sale.BatchSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface
BatchSaleRepository extends JpaRepository<BatchSale, Long> {

    List<BatchSale> findAllByApprovedFalse();

}
