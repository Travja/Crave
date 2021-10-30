package me.travja.crave.common.repositories;

import me.travja.crave.common.models.store.Store;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends CrudRepository<Store, Long> {

    List<Store> findStoresByNameIgnoreCase(String name);
    Optional<Store> findStoreByStreetAddressAndCityAndState(String streetAddress, String city, String state);

}
