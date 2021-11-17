package me.travja.crave.common.repositories;

import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.store.Store;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemsRepository       itemsRepo;
    private final ItemDetailsRepository detailsRepo;

    public ItemService(ItemsRepository itemsRepo, ItemDetailsRepository detailsRepo) {
        this.itemsRepo = itemsRepo;
        this.detailsRepo = detailsRepo;
    }


    private List<Item> clean(List<Item> list) {
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

    public List<ItemDetails> getAllDetails() {
        List<ItemDetails> items = (List<ItemDetails>) detailsRepo.findAll();
        return cleanDetails(items);
    }

    public List<Item> getAllItemsSorted() {
        List<Item> items = itemsRepo.findAllByOrderByNameAsc();
        return clean(items);
    }

    public List<Item> getAllByQuery(String query) {
        return clean(itemsRepo.findAllByQuery(query));
    }

    public List<Item> getAllByName(String name) {
        return getAllByName(name, PageRequest.of(0, 4));
    }

    public List<Item> getAllByName(String name, Pageable page) {
        return clean(itemsRepo.findAllByNameLike("%" + name + "%", page));
    }

    public List<Item> getAllFromStore(String name, long storeId, Pageable pageable) {
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
