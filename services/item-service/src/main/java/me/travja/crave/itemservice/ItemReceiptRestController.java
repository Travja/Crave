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
import me.travja.crave.common.AsyncCaller;
import me.travja.crave.common.repositories.ItemService;
import me.travja.crave.common.repositories.PendingDetailsRepository;
import me.travja.crave.common.repositories.StoreRepository;
import me.travja.crave.common.util.Formatter;
import me.travja.crave.common.views.CraveViews.DetailsView;
import org.apache.commons.lang.WordUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@CraveController("/receipt")
@Slf4j
public class ItemReceiptRestController {

    private final ItemService              itemService;
    private final PendingDetailsRepository pendingRepo;
    private final StoreRepository storeRepo;
    private final AsyncCaller     async;

    @PostMapping
    @Transactional
    @JsonView(DetailsView.class)
    public ResponseObject postReceipt(@RequestBody SimpleReceiptData data, Authentication auth) {
        List<String> authorities = auth != null
                ? auth.getAuthorities().stream().map(at -> at.getAuthority()).collect(Collectors.toList())
                : new ArrayList<>();
        try {
            System.out.println(data);
            ReceiptType type      = data.getReceiptType();
            String      storeName = WordUtils.capitalize(type.toString());
            int         updated   = 0;

            Map<ItemDetails, Double> pricesUpdated = new HashMap<>();
            List<PendingDetails>     pending       = new ArrayList<>();

            for (ProductInformation prod : data.getProductData()) {
                if (prod.getPrice() <= 0) continue;

                Item item = itemService.getItem(prod.getUpc()).orElse(new Item());
                Optional<Store> store = storeRepo.findStoreByStreetAddressAndCityAndState(data.getStreetAddress(),
                        data.getCity(), data.getState());

                store.ifPresentOrElse((stor) -> {
                    Optional<ItemDetails> details = item.getDetails(stor);
                    details.ifPresentOrElse((dets) -> {
                                double originalPrice = dets.getPrice();

                                double priceChange = dets.update(prod, authorities);

                                // EMAIL STUFF
                                //Check for big changes...
                                //If the price has increased by more than 10%, we need approval.
                                if (priceChange > originalPrice * 0.10) {
                                    PendingDetails pDetails = itemService.savePendingIfNotExists(
                                            new PendingDetails(dets.getItem(), dets.getStore(), dets.getPrice()));

                                    if (pDetails != null)
                                        pending.add(pDetails);
                                    dets.setPrice(originalPrice);
                                    log.info(dets.getItem().getName() + " changed by " + Formatter.formatCurrency(priceChange) +
                                            "! This will need some attention!");
                                } else { //This is a safe range.
                                    //Otherwise, if the price increased less than 10% or decreased in price, just put
                                    // it through and update appropriate users.
                                    if (Math.abs(priceChange) > 0.0001)
                                        pricesUpdated.put(dets, priceChange);
                                    itemService.save(dets);
                                }

                                if (stor.getLat() == 0 && stor.getLon() == 0)
                                    stor.setLocation(Location.fromAddress(data.getStreetAddress() + " " + data.getCity() + " " + data.getState()));
                            },
                            () -> item.getDetails().add(new ItemDetails(item, stor, prod.getPrice())));
                }, () -> { //Otherwise, if store is null
                    Store stor = new Store();

                    Location loc =
                            Location.fromAddress(data.getStreetAddress() + " " + data.getCity() + " " + data.getState());

                    stor.setName(storeName);
                    //TODO Standardize addresses. Maybe link to Azure results.
                    stor.setStreetAddress(data.getStreetAddress());
                    stor.setCity(data.getCity());
                    stor.setState(data.getState());
                    stor.setLocation(loc);
                    storeRepo.save(stor);

                    ItemDetails details = new ItemDetails(item, stor, prod.getPrice());
                    itemService.save(details);
                    item.getDetails().add(details);
                });

                item.update(prod, authorities);
                itemService.save(item);
                updated++;
            }

            ResponseObject res = ResponseObject.success("updated", updated, "pending", pending);
            log.info("There are " + pricesUpdated.size() + " items with new prices. And " + pending.size() +
                    " pending price updates.");
            async.handlePriceUpdates(pricesUpdated);
            async.handleApprovalNeeded(pending);
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
    public ResponseObject postItems(@RequestParam MultiValueMap<String, String> data, Authentication auth) {
        return postReceipt(new ItemizedReceiptData(data), auth);
    }

    @Transactional
    @PostMapping("/items")
    public ResponseObject postItemsJson(@RequestBody ItemizedReceiptData data, Authentication auth) {
        return postReceipt(data, auth);
    }

}
