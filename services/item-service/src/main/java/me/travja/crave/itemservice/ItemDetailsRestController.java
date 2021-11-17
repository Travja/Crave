package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.repositories.ItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static me.travja.crave.common.views.CraveViews.DetailsView;

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

}
