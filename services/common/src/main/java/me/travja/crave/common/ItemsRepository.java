package me.travja.crave.common;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Integer> {

}
