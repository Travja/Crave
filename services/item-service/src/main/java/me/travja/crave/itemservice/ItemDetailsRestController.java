package me.travja.crave.itemservice;

import me.travja.crave.common.models.ItemDetails;
import me.travja.crave.common.repositories.ItemDetailsRepository;
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
    public List<ItemDetails> getItems() {
        return (List<ItemDetails>) repo.findAll();
    }

    @PostMapping
    public ItemDetails createItem(@RequestBody ItemDetails item) {
        return repo.save(item);
    }

}
