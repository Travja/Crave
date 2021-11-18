package me.travja.crave.common.repositories;

import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.SortStrategy;
import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    //TODO Clean up this class a bunch :P

    private final ItemsRepository       itemsRepo;
    private final ItemDetailsRepository detailsRepo;

    private final Sort alphabeticalSort = Sort.by("name").ascending();

    private <T extends Iterable<Item>> T clean(T list) {
        list.forEach(Item::cleanSales);
        return list;
    }

    private List<ItemDetails> cleanDetails(List<ItemDetails> list) {
        list.forEach(ItemDetails::cleanSales);
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


    public List<Item> getAllItems() {
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

    public Page<Item> getAllItemsSorted() {
        return getAllItemsSorted(SortStrategy.ALPHABETICAL);
    }

    public Page<Item> getAllItemsSorted(SortStrategy sortStrategy) {
        return getAllItemsSorted(sortStrategy, PageRequest.of(0, 50, alphabeticalSort));
    }

    public Page<Item> getAllItemsSorted(SortStrategy sortStrategy, Pageable pageable) {
        return getAllItemsFromStoreSorted(null, -1, sortStrategy, pageable);
    }

    public Page<Item> getAllItemsFromStoreSorted(String name, long storeId, SortStrategy sortStrategy,
                                                 Pageable pageable) {
        if (name == null) name = "";

        if (storeId == -1) return getAllItems(name, sortStrategy, pageable);
        else {
            name = "%" + name.trim() + "%";
            return clean(itemsRepo.findAllByNameLikeAndDetailsStoreId(name, storeId, pageable));
//            return clean(switch (sortStrategy) {
//                case LOWEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsStoreIdAndDetailsNotEmptyOrderByDetailsPriceAsc(name,
//                        storeId,
//                        pageable);
//                case HIGHEST_FIRST -> itemsRepo.findDistinctItemAndDetailsByNameLikeAndDetailsStoreIdAndDetailsNotEmptyOrderByDetailsPriceDesc(name,
//                        storeId,
//                        pageable);
//                default -> itemsRepo.findAllByDetailsNotEmptyOrderByNameAsc(pageable);
//            });
        }
    }

    public Page<Item> getAllItemsFromStoreSorted(String name, String storeName, SortStrategy sortStrategy,
                                                 Pageable pageable) {
        if (name == null) name = "";
        if (storeName == null) storeName = "";

        name = "%" + name.trim() + "%";
        storeName = "%" + storeName.trim() + "%";
        return clean(itemsRepo.findAllByNameLikeAndDetailsStoreNameLike(name, storeName, pageable));
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

    public Page<Item> getAllItems(String name, SortStrategy sortStrategy, Pageable pageable) {
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

    public List<Item> getAllByQuery(String query) {
        return clean(itemsRepo.findAllByQuery(query));
    }

    public Page<Item> getAllByName(String name) {
        return getAllByName(name, PageRequest.of(0, 4));
    }

    public Page<Item> getAllByName(String name, Pageable page) {
        return clean(itemsRepo.findAllByNameLike("%" + name + "%", page));
    }

    public Page<Item> getAllFromStore(String name, long storeId, Pageable pageable) {
        return clean(itemsRepo.findAllByNameLikeAndDetailsStoreId("%" + name + "%", storeId, pageable));
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
