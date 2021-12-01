package me.travja.crave.saleservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.exceptions.SaleNotFoundException;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.common.models.sale.BatchSale;
import me.travja.crave.common.models.sale.Sale;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.BatchSaleRepository;
import me.travja.crave.common.repositories.SaleRepository;
import me.travja.crave.common.repositories.StoreRepository;
import me.travja.crave.common.views.CraveViews;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.travja.crave.common.views.CraveViews.SaleView;
import static me.travja.crave.common.views.CraveViews.StoreSaleView;

@Slf4j
@RequiredArgsConstructor
@CraveController("/sale")
public class SaleRestController {

    private final StoreRepository     storeRepo;
    private final SaleRepository      repo;
    private final BatchSaleRepository batchRepo;

    @GetMapping
    @JsonView(SaleView.class)
    public List<Sale> getSales(@RequestParam(required = false) Long id,
                               @RequestParam(required = false) String storeName) {
        if (id != null && id > 0)
            return repo.findAllByStoreIdAndApprovedTrue(id);
        else if (storeName != null && !storeName.isEmpty())
            return repo.findAllByStoreNameAndApprovedTrue(storeName);
        else
            return repo.findAllByApprovedTrue();
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
        return repo.findAllByItemItemUpcUpcAndApprovedTrue(upc);
    }

    @PostMapping
    @JsonView(SaleView.class)
    public ResponseObject createItem(@RequestBody BatchSale batch) {
        //TODO validate that the sale information is actually valid and not a duplicate.
        // Also notify any users that have favorited any items on this sale.
        batch.setApproved(false);
        List<Sale> created = new ArrayList<>();
        for (Sale sale : batch.getSales()) {
            created.add(repo.save(sale));
        }
        batchRepo.save(batch);
        return ResponseObject.successConditional(!created.isEmpty(), "sales", created);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(SaleView.class)
    public List<BatchSale> getPendingSales() {
        return batchRepo.findAllByApprovedFalse();
    }

    @PostMapping("/approve/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void approveSale(@PathVariable long id) {
        Optional<BatchSale> sale = batchRepo.findById(id);

        if (sale.isPresent()) {
            BatchSale s = sale.get();
            repo.saveAll(s.getSales());
            s.approve();
            batchRepo.deleteById(s.getId());
            log.info("Batch sale " + id + " approved.");
        } else {
            throw new SaleNotFoundException("Sale by id " + id + " does not exist.");
        }
    }

    @PostMapping("/reject/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void rejectSale(@PathVariable long id) {
        Optional<BatchSale> sale = batchRepo.findById(id);

        if (sale.isPresent()) {
            BatchSale s = sale.get();
            repo.deleteAll(s.getSales());
            batchRepo.deleteById(s.getId());
            log.info("Batch sale " + id + " rejected.");
        } else {
            throw new SaleNotFoundException("Sale by id " + id + " does not exist.");
        }
    }

    //TODO Create update (PATCH) method

}
