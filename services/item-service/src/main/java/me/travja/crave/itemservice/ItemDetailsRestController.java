package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import me.travja.crave.common.models.ItemDetails;
import me.travja.crave.common.repositories.ItemDetailsRepository;
import me.travja.crave.common.views.DetailsView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-details")
public class ItemDetailsRestController {

    private final ItemDetailsRepository repo;

    public ItemDetailsRestController(ItemDetailsRepository repo) {
        this.repo = repo;
    }

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
