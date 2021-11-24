package me.travja.crave.common.repositories;

import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.models.SortStrategy;
import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.item.PendingDetails;
import me.travja.crave.common.models.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public record ItemService(ItemsRepository itemsRepo,
                          ItemDetailsRepository detailsRepo,
                          PendingDetailsRepository pendingRepo) {

    //TODO Clean up this class a bunch :P

    private <T extends Iterable<Item>> T clean(T list) {
        list.forEach(itm -> {
            itm.cleanSales();
            cleanDetails(itm.getDetails());
        });
        return list;
    }

    private Set<ItemDetails> cleanDetails(Set<ItemDetails> list) {
        list.forEach(ItemDetails::cleanSales);
//        list.removeAll(
//                list.stream()
//                        .filter(det -> det instanceof PendingDetails)
//                        .collect(Collectors.toList())
//        );
        return list;
    }

    private List<ItemDetails> cleanDetails(List<ItemDetails> list) {
        list.forEach(ItemDetails::cleanSales);
//        list.removeAll(
//                list.stream()
//                        .filter(det -> det instanceof PendingDetails)
//                        .collect(Collectors.toList())
//        );
        return list;
    }

    public Item save(Item item) {
        return itemsRepo.save(item);
    }

    public ItemDetails save(ItemDetails details) {
        details.setLastUpdated(new Date());
        return detailsRepo.save(details);
    }

    public List<Item> save(List<Item> list) {
        return itemsRepo.saveAll(list);
    }

    public List<ItemDetails> saveDetails(List<ItemDetails> list) {
        return (List<ItemDetails>) detailsRepo.saveAll(list);
    }


    public List<Item> getAllItemsUnsorted() {
        List<Item> items = itemsRepo.findAll();
        return clean(items);
    }

    public List<ItemDetails> getDetails(String storeName) {
        return cleanDetails(detailsRepo.findAllByStoreNameLike(storeName));
    }

    public List<ItemDetails> getDetails(long storeId) {
        return cleanDetails(detailsRepo.findAllByStoreId(storeId));
    }

    public List<ItemDetails> getAllDetails() {
        List<ItemDetails> items = (List<ItemDetails>) detailsRepo.findAll();
        return cleanDetails(items);
    }

    public Page<Item> getAllItems() {
        return getAllItems(SortStrategy.ALPHABETICAL);
    }

    public Page<Item> getAllItems(SortStrategy sortStrategy) {
        return getAllItems(sortStrategy, PageRequest.of(0, 50,
                sortStrategy.getSort().and(SortStrategy.ALPHABETICAL.getSort())));
    }

    public Page<Item> getAllItems(SortStrategy sortStrategy, Pageable pageable) {
        return getAllFromStore(null, -1, sortStrategy, pageable);
    }

    public Page<Item> getAllItems(String name, Pageable pageable) {
        if (name == null) name = "";
        name = "%" + name.trim() + "%";

        return itemsRepo.findAllByNameLike(name, pageable);
//        return getAllItemsFromStoreSorted(name, "", sortStrategy, pageable);
//        return clean(switch (sortStrategy) {
//            case LOWEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsNotEmptyOrderByDetailsPriceAsc(name, pageable);
//            case HIGHEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsNotEmptyOrderByDetailsPriceDesc(name, pageable);
//            default -> itemsRepo.findAllByDetailsNotEmptyOrderByNameAsc(pageable);
//        });
    }

    public Page<Item> getAllByName(String name) {
        return getAllByName(name, PageRequest.of(0, 4));
    }

    public Page<Item> getAllByName(String name, Pageable page) {
        return clean(itemsRepo.findAllByNameLike("%" + name + "%", page));
    }

    public List<Item> getAllByQuery(String query) {
        return clean(itemsRepo.findAllByQuery(query));
    }

    public Page<Item> getAllFromStore(String name, Object store, SortStrategy sortStrategy,
                                      Pageable pageable) {
        if (name == null) name = "";
        name = "%" + name.trim() + "%";

        if (store instanceof String || store == null) {
            if (store == null) store = ".*";
            else store = "(" + String.join("|", ((String) store).trim().split(",")) + ")";
        }

        log.info("*********************");
        log.info("Page size: " + pageable.getPageSize() + " --- Offset: " + pageable.getOffset());
        List<Long> itemIds = getIds(name, store, sortStrategy, pageable);

        log.info("Found " + itemIds + " items.");
        log.info(String.join(", ", itemIds.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));

        long count = store instanceof String ?
                itemsRepo.countByNameAndStore(name, (String) store) :
                itemsRepo.countDistinctIdByNameLikeAndDetailsStoreId(name, (Long) store);
        return new PageImpl(getItems(itemIds, pageable).getContent(), pageable, count);
    }

    private List<Long> getIds(String name, Object store, SortStrategy sortStrategy, Pageable pageable) {
        if (store instanceof String) {
            if (store == null) store = ".*";
            else store = "(" + String.join("|", ((String) store).trim().split(",")) + ")";
        }

        return switch (sortStrategy) {
            case ALPHABETICAL -> {
                if (store instanceof String)
                    yield itemsRepo.findIds(name, (String) store, pageable.getPageSize(), pageable.getOffset());
                else
                    yield itemsRepo.findIds(name, (Long) store, pageable.getPageSize(), pageable.getOffset());
            }
            case LOWEST_FIRST -> {
                if (store instanceof String)
                    yield itemsRepo.findIdsLowestFirst(name, (String) store, pageable.getPageSize(), pageable.getOffset());
                else
                    yield itemsRepo.findIdsLowestFirst(name, (Long) store, pageable.getPageSize(), pageable.getOffset());
            }
            case HIGHEST_FIRST -> {
                if (store instanceof String)
                    yield itemsRepo.findIdsHighestFirst(name, (String) store, pageable.getPageSize(), pageable.getOffset());
                else
                    yield itemsRepo.findIdsHighestFirst(name, (Long) store, pageable.getPageSize(), pageable.getOffset());
            }
        };
    }

    public Page<Item> getAllFromStore(String name, String store, double lat, double lon,
                                      double distance, SortStrategy sortStrategy, Pageable pageable) {
        if (name == null) name = "";
        name = "%" + name.trim() + "%";

        if (store == null) store = ".*";
        else store = "(" + String.join("|", store.trim().split(",")) + ")";

        log.info("*********************");
        log.info("Page size: " + pageable.getPageSize() + " --- Offset: " + pageable.getOffset());
        List<Long> itemIds = getIds(name, store, lat, lon, distance, sortStrategy, pageable);

        log.info("Found " + itemIds + " items.");
        log.info(String.join(", ", itemIds.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));

        long count = itemsRepo.countByNameAndStoreWithDistance(name, store, lat, lon, distance);
        return new PageImpl(getItems(itemIds, pageable).getContent(), pageable, count);
    }

    private List<Long> getIds(String name, String store, double lat, double lon,
                              double distance, SortStrategy sortStrategy, Pageable pageable) {
        if (store == null) store = ".*";
        else if (!store.startsWith(".") && !store.startsWith("("))
            store = "(" + String.join("|", store.trim().split(",")) + ")";

        return switch (sortStrategy) {
            case ALPHABETICAL -> itemsRepo.findIds(name, store, lat, lon, distance, pageable.getPageSize(), pageable.getOffset());
            case LOWEST_FIRST -> itemsRepo.findIdsLowestFirst(name, store, lat, lon, distance, pageable.getPageSize(), pageable.getOffset());
            case HIGHEST_FIRST -> itemsRepo.findIdsHighestFirst(name, store, lat, lon, distance, pageable.getPageSize(), pageable.getOffset());
        };
    }

    public Page<Item> getItems(List<Long> ids, Pageable pageable) {
        log.info("Querying db for items with the appropriate ids.");
        Page<Item> items = itemsRepo.findAllByIdIn(ids.stream().collect(Collectors.toList()), PageRequest.of(0,
                pageable.getPageSize(), pageable.getSort()));
        log.info("Found " + items.getNumberOfElements() + " items.");
        log.info("*********************");
        return clean(items);
    }

    public Optional<Item> getItem(String upc) {
        Optional<Item> item = itemsRepo.findByUpcUpc(upc);
        item.ifPresent(it -> it.cleanSales());

        return item;
    }

    public List<ItemDetails> getDetailsByName(String name) {
        return cleanDetails(detailsRepo.findAllByItemNameLike("%" + name + "%"));
    }

    public List<ItemDetails> getItemDetails(String upc) {
        List<ItemDetails> dets = detailsRepo.findAllByItemUpcUpc(upc);
        return cleanDetails(dets);
    }

    public Optional<ItemDetails> getItemDetails(long detailsId) {
        return detailsRepo.findById(detailsId);
    }

    public Optional<ItemDetails> getItemDetails(long itemId, long storeId) {
        log.info(itemId + " " + storeId);
        Optional<ItemDetails> items = detailsRepo.findByItemIdAndStoreId(itemId, storeId);
        log.info(items.toString());

        return items;
    }

    public Optional<ItemDetails> getItemDetails(String upc, long storeId) {
        Optional<ItemDetails> dets = detailsRepo.findByItemUpcUpcAndStoreId(upc, storeId);
        dets.ifPresent(det -> det.getClass());

        return dets;
    }

    public Optional<ItemDetails> getCheapestAtStore(String name, Store store) {
        return detailsRepo.findFirstByItemNameLikeAndStoreIdOrderBySalesNewPriceAscPriceAsc("%" + name + "%",
                store.getId());
    }

    public Optional<ItemDetails> getFirstCheapest(String name) {
        return detailsRepo.findFirstByItemNameLikeOrderBySalesNewPriceAscPriceAsc("%" + name + "%");
    }

    public List<PendingDetails> getAllPending() {
        return pendingRepo.findAll();
    }

    public PendingDetails getPending(long id) {
        return pendingRepo.findById(id).orElse(null);
    }

    public void delete(ItemDetails details) {
        detailsRepo.deleteById(details.getId());
    }

    public void delete(PendingDetails details) {
        pendingRepo.deleteById(details.getId());
    }

}
