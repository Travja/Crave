package me.travja.crave.receiptservice;

import me.travja.crave.receiptservice.models.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Integer> {

}
