package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.ItemDetails;
import me.travja.crave.common.repositories.ItemDetailsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static me.travja.crave.common.views.CraveViews.DetailsView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item-details")
public class ItemDetailsRestController {

    private final ItemDetailsRepository repo;

    @GetMapping
    @JsonView(DetailsView.class)
//    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ItemDetails> getItems() {
        return (List<ItemDetails>) repo.findAll();
    }

    @GetMapping("/{upc}")
    @JsonView(DetailsView.class)
    public List<ItemDetails> getItem(@PathVariable String upc) {
        return repo.findAllByItemUpcUpc(upc);
    }

    @PostMapping
    @JsonView(DetailsView.class)
    public ItemDetails createItem(@RequestBody ItemDetails item) {
        return repo.save(item);
    }

}
