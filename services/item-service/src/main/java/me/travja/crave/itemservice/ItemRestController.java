package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.models.Item;
import me.travja.crave.common.models.RequestItem;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.common.views.ItemView;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @PatchMapping("/{upc}")
    @JsonView(ItemView.class)
    public Item patchOrCreateItem(@PathVariable String upc, @RequestBody RequestItem item, Authentication auth) {
        Optional<Item> it = repo.findByUpcUpc(upc);
        if (it.isPresent()) {
            List<String> authorities = auth != null
                    ? auth.getAuthorities().stream().map(at -> at.getAuthority()).collect(Collectors.toList())
                    : new ArrayList<>();

            //Update item
            Item itm = it.get();
            itm.setUpc(upc);
            if (item.getDescription() != null)
                itm.setDescription(item.getDescription());
            if (item.getImage() != null && (itm.getImage() == null || authorities.contains("ADMIN")))
                itm.setImage(item.getImage());
            if (item.getName() != null && (itm.getName() == null || authorities.contains("ADMIN")))
                itm.setName(item.getName());
            return repo.save(itm);
        } else {
            return repo.save(item.toItem());
        }
    }

}
