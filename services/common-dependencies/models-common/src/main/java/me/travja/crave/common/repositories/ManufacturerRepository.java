package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Manufacturer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturerRepository extends CrudRepository<Manufacturer, String> {
}
