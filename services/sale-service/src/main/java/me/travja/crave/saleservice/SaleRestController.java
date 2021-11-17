package me.travja.crave.saleservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.common.models.item.Sale;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.SaleRepository;
import me.travja.crave.common.repositories.StoreRepository;
import me.travja.crave.common.repositories.UserRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static me.travja.crave.common.views.CraveViews.SaleView;
import static me.travja.crave.common.views.CraveViews.StoreSaleView;

@RequiredArgsConstructor
@CraveController("/sale")
public class SaleRestController {

    private final StoreRepository storeRepo;
    private final SaleRepository  repo;
    private final UserRepo        userRepo;

    @GetMapping
    @JsonView(SaleView.class)
    public List<Sale> getSales(@RequestParam(required = false) Long id,
                               @RequestParam(required = false) String storeName) {
        if (id != null && id > 0)
            return repo.findAllByStoreId(id);
        else if (storeName != null && !storeName.isEmpty())
            return repo.findAllByStoreName(storeName);
        else
            return repo.findAll();
    }

    @GetMapping("/store/{storeId}")
    @JsonView(StoreSaleView.class)
    public Store getSalesByStore(@PathVariable long storeId) {
        return storeRepo.findById(storeId).orElse(null);
    }

    @GetMapping("/search")
    @JsonView(SaleView.class)
    public List<Sale> searchSales(@RequestParam(required = false) String query,
                                  @RequestParam(required = false, defaultValue = "0") int page,
                                  @RequestParam(required = false, defaultValue = "4") int count) {
        if (query == null || query.isEmpty()) return Collections.emptyList();

        List<Sale> sales = repo.findAllByQuery(query, PageRequest.of(page, count));

        return sales;
    }

    @GetMapping("/upc/{upc}")
    @JsonView(SaleView.class)
    public List<Sale> getSaleByUPC(@PathVariable String upc) {
        return repo.findAllByItemItemUpcUpc(upc);
    }

    @PostMapping
    @JsonView(SaleView.class)
    public ResponseObject createItem(@RequestBody Sale sale) {
        //TODO validate that the sale information is actually valid and not a duplicate.
        // Also notify any users that have favorited any items on this sale.
        Sale s = repo.save(sale);
        return ResponseObject.successConditional(s != null, "sale", s);
    }

    //TODO Create update (PATCH) method

}
