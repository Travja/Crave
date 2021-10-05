package me.travja.crave.itemservice;

import me.travja.crave.itemservice.models.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Integer> {

}
