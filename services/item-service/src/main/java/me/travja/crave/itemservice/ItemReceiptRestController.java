package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.common.models.item.*;
import me.travja.crave.common.models.item.SimpleReceiptData.ReceiptType;
import me.travja.crave.common.models.store.Location;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.ItemService;
import me.travja.crave.common.repositories.PendingDetailsRepository;
import me.travja.crave.common.repositories.StoreRepository;
import me.travja.crave.common.util.Formatter;
import me.travja.crave.common.views.CraveViews.DetailsView;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@RequiredArgsConstructor
@CraveController("/receipt")
@Slf4j
public class ItemReceiptRestController {

    private final ItemService              itemService;
    private final PendingDetailsRepository pendingRepo;
    private final StoreRepository          storeRepo;
    private final AsyncCaller              async;

    @PostMapping
    @Transactional
    @JsonView(DetailsView.class)
    public ResponseObject postReceipt(@RequestBody SimpleReceiptData data) {
        try {
            System.out.println(data);
            ReceiptType type      = data.getReceiptType();
            String      storeName = WordUtils.capitalize(type.toString());
            int         updated   = 0;

            Map<ItemDetails, Double> pricesUpdated = new HashMap<>();
            List<PendingDetails>     pending       = new ArrayList<>();

            for (ProductInformation prod : data.getProductData()) {
                if(prod.getPrice() <= 0) continue;
                
                Item item = itemService.getItem(prod.getUpc()).orElse(new Item());
                Optional<Store> store = storeRepo.findStoreByStreetAddressAndCityAndState(data.getStreetAddress(),
                        data.getCity(), data.getState());

                store.ifPresentOrElse((stor) -> {
                    Optional<ItemDetails> details = item.getDetails(stor);
                    details.ifPresentOrElse((dets) -> {
                                double originalPrice = dets.getPrice();

                                double priceChange = dets.update(prod);

                                //Check for big changes...
                                if (priceChange > originalPrice * 0.10) {
                                    pending.add(pendingRepo.save(new PendingDetails(dets.getItem(), dets.getStore(),
                                            dets.getPrice())));
                                    dets.setPrice(originalPrice);
                                    log.info(dets.getItem().getName() + " changed by " + Formatter.formatCurrency(priceChange) +
                                            "! This will need some attention!");
                                } else { //This is a safe range.
                                    if (Math.abs(priceChange) > 0.0001)
                                        pricesUpdated.put(dets, priceChange);
                                    itemService.save(dets);
                                }
                            },
                            () -> item.getDetails().add(new ItemDetails(item, stor, prod.getPrice())));
                }, () -> { //Otherwise, if store is null
                    Store stor = new Store();

                    stor.setName(storeName);
                    stor.setStreetAddress(data.getStreetAddress());
                    stor.setCity(data.getCity());
                    stor.setState(data.getState());
                    //TODO Get the actual Lat/Lon for the store and save that too
                    stor.setLocation(new Location());//new Location(lat, lon));
                    storeRepo.save(stor);

                    ItemDetails details = new ItemDetails(item, stor, prod.getPrice());
                    itemService.save(details);
                    item.getDetails().add(details);
                });

                item.update(prod);
                itemService.save(item);
                updated++;
            }

            ResponseObject res = ResponseObject.success("updated", updated, "pending", pending);
            log.info("There are " + pricesUpdated.size() + " items with new prices. And " + pending.size() +
                    " pending price updates.");
            async.handlePriceUpdates(pricesUpdated);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseObject.failure("error", e.getMessage());
        }
    }

    @Transactional
    @PostMapping(path = "/web/items", consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseObject postItems(@RequestParam MultiValueMap<String, String> data) {
        return postReceipt(new ItemizedReceiptData(data));
    }

    @Transactional
    @PostMapping("/items")
    public ResponseObject postItemsJson(@RequestBody ItemizedReceiptData data) {
        return postReceipt(data);
    }

}
