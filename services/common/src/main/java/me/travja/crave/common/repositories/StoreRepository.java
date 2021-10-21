package me.travja.crave.common.repositories;

import me.travja.crave.common.models.Store;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends CrudRepository<Store, Long> {

    Optional<Store> findStoreByNameIgnoreCase(String name);

}
