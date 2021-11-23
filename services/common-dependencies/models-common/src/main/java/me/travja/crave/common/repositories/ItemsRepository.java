package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.util.Queries;
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
    @Query(value = Queries.Count.byNameStore, nativeQuery = true)
    long countByNameAndStore(String name, String storeName);
    @Query(value = Queries.Count.byNameStoreDistance, nativeQuery = true)
    long countByNameAndStoreWithDistance(String name, String storeName, double lat, double lon, double distance);

    Optional<Item> findByUpcUpc(String upc);

    @Query("from Item i where i.name like %:query% or i.description like %:query% " +
            "or i.upc.upc like %:query% order by i.name asc")
    List<Item> findAllByQuery(String query);

    Page<Item> findAll(Pageable pageable);
    Page<Item> findAllByNameLike(String name, Pageable pageable);

    @Query(value = Queries.Ids.ByName.byNameStore, nativeQuery = true)
    List<Long> findIds(String name, String storeName, int limit, long offset);
    @Query(value = Queries.Ids.ByName.byNameStoreId, nativeQuery = true)
    List<Long> findIds(String name, long storeId, int limit, long offset);
    @Query(value = Queries.Ids.ByName.byNameStoreDistance, nativeQuery = true)
    List<Long> findIds(String name, String storeName, double lat, double lon, double distance, int limit, long offset);

    @Query(value = Queries.Ids.ByLowest.byNameAndStore, nativeQuery = true)
    List<Long> findIdsLowestFirst(String name, String storeName, int limit, long offset);
    @Query(value = Queries.Ids.ByHighest.byNameandStore, nativeQuery = true)
    List<Long> findIdsHighestFirst(String name, String storeName, int limit, long offset);

    @Query(value = Queries.Ids.ByLowest.byNameAndStoreId, nativeQuery = true)
    List<Long> findIdsLowestFirst(String name, long storeId, int limit, long offset);
    @Query(value = Queries.Ids.ByHighest.byNameAndStoreId, nativeQuery = true)
    List<Long> findIdsHighestFirst(String name, long storeId, int limit, long offset);

    @Query(value = Queries.Ids.ByLowest.byNameStoreDistance, nativeQuery = true)
    List<Long> findIdsLowestFirst(String name, String storeName, double lat, double lon, double distance,
                                  int limit, long offset);
    @Query(value = Queries.Ids.ByHighest.byNameStoreDistance, nativeQuery = true)
    List<Long> findIdsHighestFirst(String name, String storeName, double lat, double lon, double distance,
                                   int limit, long offset);

    /**
     * Get the items by a list of ids. The {@link Pageable} passed in should be unpaged and used for Sorting only.
     * @param ids - The list of ids to fetch.
     * @param pageable - Please use Unpaged or just a pageable at page 0 to get appropriate results. This should be
     *                 used for sorting only.
     * @return the {@link Page} representing the response.
     */
    Page<Item> findAllByIdIn(List<Long> ids, Pageable pageable);

}
