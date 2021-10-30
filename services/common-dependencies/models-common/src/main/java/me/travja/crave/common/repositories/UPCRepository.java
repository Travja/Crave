package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.UPC;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UPCRepository extends CrudRepository<UPC, String> {
}
