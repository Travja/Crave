package me.travja.crave.common.repositories;

import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    //TODO Clean up this class a bunch :P

    private final ItemsRepository       itemsRepo;
    private final ItemDetailsRepository detailsRepo;

    private <T extends Iterable<Item>> T clean(T list) {
        list.forEach(itm -> {
            itm.cleanSales();
            cleanDetails(itm.getDetails());
        });
        return list;
    }

    private Set<ItemDetails> cleanDetails(Set<ItemDetails> list) {
        list.forEach(ItemDetails::cleanSales);
        list.removeAll(
                list.stream()
                        .filter(det -> det instanceof PendingDetails)
                        .collect(Collectors.toList())
        );
        return list;
    }

    private List<ItemDetails> cleanDetails(List<ItemDetails> list) {
        list.forEach(ItemDetails::cleanSales);
        list.removeAll(
                list.stream()
                        .filter(det -> det instanceof PendingDetails)
                        .collect(Collectors.toList())
        );
        return list;
    }

    public Item save(Item item) {
        return itemsRepo.save(item);
    }

    public ItemDetails save(ItemDetails details) {
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

    public Page<Item> getAllFromStore(String name, long storeId, SortStrategy sortStrategy,
                                      Pageable pageable) {
        if (name == null) name = "";

        if (storeId == -1) return getAllItems(name, pageable);
        else {
            name = "%" + name.trim() + "%";
            List<Long> itemIds = switch (sortStrategy) {
                case ALPHABETICAL -> itemsRepo.findIds(name, storeId, pageable.getPageSize(), pageable.getOffset());
                case LOWEST_FIRST -> itemsRepo.findIdsLowestFirst(name, storeId, pageable.getPageSize(), pageable.getOffset());
                case HIGHEST_FIRST -> itemsRepo.findIdsHighestFirst(name, storeId, pageable.getPageSize(), pageable.getOffset());
            };

            long       count = itemsRepo.countDistinctIdByNameLikeAndDetailsStoreId(name, storeId);
            List<Item> items = itemsRepo.findAllByIdIn(itemIds.stream().collect(Collectors.toList()));
            Page       p     = new PageImpl(items, pageable, count);
            return clean(p);
        }
    }

    public Page<Item> getAllFromStore(String name, String storeName, SortStrategy sortStrategy,
                                      Pageable pageable) {
        if (name == null) name = "";
        if (storeName == null) storeName = "";

        name = "%" + name.trim() + "%";
        storeName = "%" + storeName.trim() + "%";

        log.info("*********************");
        log.info("Page size: " + pageable.getPageSize() + " --- Offset: " + pageable.getOffset());
        List<Long> itemIds = switch (sortStrategy) {
            case ALPHABETICAL -> itemsRepo.findIds(name, storeName, pageable.getPageSize(), pageable.getOffset());
            case LOWEST_FIRST -> itemsRepo.findIdsLowestFirst(name, storeName, pageable.getPageSize(), pageable.getOffset());
            case HIGHEST_FIRST -> itemsRepo.findIdsHighestFirst(name, storeName, pageable.getPageSize(), pageable.getOffset());
        };

        log.info("Found " + itemIds + " items.");
        log.info(String.join(", ", itemIds.stream().map(i -> String.valueOf(i)).collect(Collectors.toList())));

        long count = itemsRepo.countDistinctIdByNameLikeAndDetailsStoreNameLike(name, storeName);
        log.info("Querying db for items with the appropriate ids.");
        List<Item> items = itemsRepo.findAllByIdIn(itemIds.stream().collect(Collectors.toList()));
        log.info("Found " + items.size() + " items.");
        log.info("*********************");
        Page p = new PageImpl(items, pageable, count);
        return clean(p);
//            return clean(switch (sortStrategy) {
//                case LOWEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsStoreNameAndDetailsNotEmptyOrderByDetailsPriceAsc(name,
//                        storeName,
//                        pageable);
//                case HIGHEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsStoreNameAndDetailsNotEmptyOrderByDetailsPriceDesc(name,
//                        storeName,
//                        pageable);
//                default -> itemsRepo.findAllByDetailsNotEmptyOrderByNameAsc(pageable);
//            });
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

    public Optional<ItemDetails> getItemDetails(String upc, long storeId) {
        Optional<ItemDetails> dets = detailsRepo.findByItemUpcUpcAndStoreId(upc, storeId);
        dets.ifPresent(det -> det.getClass());

        return dets;
    }

    public Optional<ItemDetails> getCheapestAtStore(String name, Store store) {
        return detailsRepo.findFirstByItemNameLikeAndStoreOrderBySalesNewPriceAscPriceAsc("%" + name + "%", store);
    }

    public Optional<ItemDetails> getFirstCheapest(String name) {
        return detailsRepo.findFirstByItemNameLikeOrderBySalesNewPriceAscPriceAsc("%" + name + "%");
    }

    public void filter() {

    }

}
