package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.Item;
import me.travja.crave.common.models.RequestItem;
import me.travja.crave.common.models.UPC;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.common.views.ItemView;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final ItemsRepository repo;

    @GetMapping
    @JsonView(ItemView.class)
    public List<Item> getItems() {
        return (List<Item>) repo.findAll();
    }

    @PostMapping
    @JsonView(ItemView.class)
    public Item createItem(@RequestBody RequestItem item) {
        return repo.save(item.toItem());
    }

    @PatchMapping
    @JsonView(ItemView.class)
    public Item patchOrCreateItem(@RequestBody RequestItem item) {
        UPC            upc = item.toItem().getUpc();
        Optional<Item> it  = repo.findByUpcUpc(upc.getUPC());
        if (it.isPresent()) {
            //Update item
            Item itm = it.get();
            itm.setUpc(upc);
            if (item.getDescription() != null)
                itm.setDescription(item.getDescription());
            if (item.getImage() != null && itm.getImage() == null)
                itm.setImage(item.getImage());
            if (item.getName() != null && itm.getName() == null)
                itm.setName(item.getName());
            return repo.save(itm);
        } else {
            return repo.save(item.toItem());
        }
    }

}
