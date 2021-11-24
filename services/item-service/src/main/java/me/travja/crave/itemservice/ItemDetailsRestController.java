package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.common.repositories.ItemService;
import me.travja.crave.common.views.CraveViews;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.travja.crave.common.views.CraveViews.DetailsView;

@Slf4j
@RequiredArgsConstructor
@CraveController("/item-details")
public class ItemDetailsRestController {

    private final ItemService itemService;

    @GetMapping
    @JsonView(DetailsView.class)
//    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ItemDetails> getItems() {
        return itemService.getAllDetails();
    }

    @GetMapping("/{upc}")
    @JsonView(DetailsView.class)
    public List<ItemDetails> getItem(@PathVariable String upc) {
        return itemService.getItemDetails(upc);
    }

    @PostMapping
    @JsonView(DetailsView.class)
    public ItemDetails createItem(@RequestBody ItemDetails item) {
        return itemService.save(item);
    }

    @PatchMapping("/{detailsId}")
    @JsonView(CraveViews.DetailsView.class)
    public ItemDetails patchOrCreateItem(@PathVariable long detailsId, @RequestBody ItemDetails details,
                                         Authentication auth) {
        Optional<ItemDetails> storedDetails = itemService.getItemDetails(detailsId);

        if (storedDetails.isPresent()) {
            List<String> authorities = auth != null
                    ? auth.getAuthorities().stream().map(at -> at.getAuthority()).collect(Collectors.toList())
                    : new ArrayList<>();

            //Update item
            ItemDetails dets = storedDetails.get();
            Item        itm  = details.getItem();
            if (itm == null) return null;

            dets.setInStock(details.isInStock());
            dets.setCarried(details.isCarried());

            log.info(itm.getDescription());
            dets.update(new ProductInformation(itm.getName(), null, itm.getImage(), itm.getDescription(),
                    details.getPrice()), authorities);

            itemService.save(dets.getItem());
            return itemService.save(dets);
        } else {
            return itemService.save(details);
        }
    }

    @PatchMapping("/multiple")
    @JsonView(CraveViews.DetailsView.class)
    public List<ItemDetails> updateMultiple(@RequestBody List<ItemDetails> details, Authentication auth) {
        return details.stream().map(dets -> patchOrCreateItem(dets.getId(), dets, auth)).collect(Collectors.toList());
    }

}
