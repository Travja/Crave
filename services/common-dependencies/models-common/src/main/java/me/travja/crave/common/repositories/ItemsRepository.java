package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Item, Long> {

    long countDistinctIdByNameLikeAndDetailsStoreId(String name, long storeId);
    long countDistinctIdByNameLikeAndDetailsStoreNameLike(String name, String storeName);

    Optional<Item> findByUpcUpc(String upc);

    @Query("from Item i where i.name like %:query% or i.description like %:query% " +
            "or i.upc.upc like %:query% order by i.name asc")
    List<Item> findAllByQuery(String query);

    Page<Item> findAll(Pageable pageable);
    Page<Item> findAllByNameLike(String name, Pageable pageable);

    @Query(value =
            "select distinct id from (" +
                    "  select i.id, min(i.name) as name" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "  where i.name like :name" +
                    "    and s.name like :storeName" +
                    "  group by i.id" +
                    "  order by name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIds(String name, String storeName, int limit, long offset);
    @Query(value =
            "select distinct id from (" +
                    "  select i.id, min(i.name)" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "  where i.name like :name" +
                    "    and s.id like :storeId" +
                    "  group by i.id" +
                    "  order by name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIds(String name, long storeId, int limit, long offset);

    @Query(value =
            "select distinct id from (" +
                    "  select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "    left outer join sale s2" +
                    "      on d.id = s2.item_id" +
                    "  where i.name like :name" +
                    "    and s.name like :storeName" +
                    "    and (s2.new_price is null or s2.end_date > CURRENT_DATE)" +
                    "  group by i.id" +
                    "  order by sale asc, min asc, name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIdsLowestFirst(String name, String storeName, int limit, long offset);
    @Query(value =
            "select distinct id from (" +
                    "  select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "    left outer join sale s2" +
                    "      on d.id = s2.item_id" +
                    "  where i.name like :name" +
                    "    and s.name like :storeName" +
                    "    and (s2.new_price is null or s2.end_date > CURRENT_DATE)" +
                    "  group by i.id" +
                    "  order by sale desc, min desc, name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIdsHighestFirst(String name, String storeName, int limit, long offset);

    @Query(value =
            "select distinct id from (" +
                    "  select i.id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "    left outer join sale s2" +
                    "      on d.id = s2.item_id" +
                    "  where i.name like :name" +
                    "    and s.id like :storeId" +
                    "    and (s2.new_price is null or s2.end_date > CURRENT_DATE)" +
                    "  group by i.id" +
                    "  order by sale asc, min asc, name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIdsLowestFirst(String name, long storeId, int limit, long offset);
    @Query(value =
            "select distinct id from (" +
                    "  select i.id as id, min(i.name) as name, min(s2.new_price) as sale, min(d.price) as min" +
                    "  from item i" +
                    "    left outer join item_details d" +
                    "      on d.item_id = i.id" +
                    "    left outer join store s" +
                    "      on s.id = d.store_id" +
                    "    left outer join sale s2" +
                    "      on d.id = s2.item_id" +
                    "  where i.name like :name" +
                    "    and s.id = :storeId" +
                    "    and (s2.new_price is null or s2.end_date > CURRENT_DATE)" +
                    "  group by i.id" +
                    "  order by sale desc, min desc, name asc" +
                    "  limit :limit" +
                    "  offset :offset" +
                    ") r",
            nativeQuery = true)
    List<Long> findIdsHighestFirst(String name, long storeId, int limit, long offset);

    List<Item> findAllByIdIn(List<Long> ids);

}
