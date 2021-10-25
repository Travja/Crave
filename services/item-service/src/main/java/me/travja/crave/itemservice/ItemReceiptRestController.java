package me.travja.crave.itemservice;

import lombok.AllArgsConstructor;
import me.travja.crave.common.models.*;
import me.travja.crave.common.models.SimpleReceiptData.ReceiptType;
import me.travja.crave.common.repositories.ItemDetailsRepository;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.common.repositories.StoreRepository;
import org.apache.commons.lang.WordUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/receipt")
public class ItemReceiptRestController {

    private final ItemsRepository       itemRepo;
    private final ItemDetailsRepository itemDetailsRepo;
    private final StoreRepository       storeRepo;

    @PostMapping
    @Transactional
    public ResponseObject postReceipt(@RequestBody SimpleReceiptData data) {
        try {
            ReceiptType type      = data.getReceiptType();
            String      storeName = WordUtils.capitalize(type.toString());
            int         updated   = 0;

            for (ProductInformation prod : data.getProductData()) {
                Item                  item    = itemRepo.findByUpcUpc(prod.getUpc()).orElse(new Item());
                Store                 store   = storeRepo.findStoreByNameIgnoreCase(storeName).orElse(null);
                Optional<ItemDetails> details = item.getDetails(store);
                details.ifPresentOrElse((dets) -> dets.update(prod),
                        () -> item.getDetails().add(new ItemDetails(item, store, prod.getPrice())));

                item.update(prod);
                itemRepo.save(item);
                updated++;
            }

            ResponseObject res = ResponseObject.success("updated", updated);
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
