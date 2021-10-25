package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.filters.AuthToken;
import me.travja.crave.common.models.CraveUser;
import me.travja.crave.common.models.Item;
import me.travja.crave.common.models.RequestItem;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.common.repositories.ItemsRepository;
import me.travja.crave.common.repositories.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.travja.crave.common.views.CraveViews.ItemView;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final ItemsRepository repo;
    private final UserRepo        userRepo;

    @GetMapping
    @JsonView(ItemView.class)
    public List<Item> getItems(@RequestParam(required = false) String query, Authentication auth) {
        List<Item> items;
        if (query == null)
            items = (List<Item>) repo.findAll();
        else
            items = repo.findAllByQuery(query);

        if (auth != null) {
            Optional<CraveUser> user = userRepo.findByUsernameIgnoreCase(auth.getName());
            user.ifPresent(usr ->
                    items.stream()
                            .filter(itm -> usr.getFavorites().contains(itm))
                            .forEach(itm -> itm.setFavorite(true))
            );
        }

        return items;
    }

    @GetMapping("/{upc}")
    @JsonView(ItemView.class)
    public Item getItem(@PathVariable String upc, Authentication auth) {
        Item item = repo.findByUpcUpc(upc).orElse(null);

        if (item != null && auth != null)
            userRepo.findByUsernameIgnoreCase(auth.getName())
                    .ifPresent(user -> item.setFavorite(user.getFavorites().contains(item)));

        return item;
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

    @PostMapping("/favorite/{upc}")
    public ResponseObject favorite(@PathVariable String upc, Authentication auth) {
        System.out.println(auth);
        if (auth instanceof AuthToken) {
            CraveUser user = userRepo.findByUsernameIgnoreCase(auth.getName()).orElse(null);
            Item      item = repo.findByUpcUpc(upc).orElse(null);
            if (user == null)
                return ResponseObject.failure("error", "User not authenticated (Or not found).");
            else if (item == null)
                return ResponseObject.failure("error", "Item not found (" + upc + ")");
            else {
                if (!user.getFavorites().contains(item))
                    user.getFavorites().add(item);
                userRepo.save(user);
                return ResponseObject.success();
            }
        }

        return ResponseObject.failure();
    }

    @PostMapping("/unfavorite/{upc}")
    public ResponseObject unfavorite(@PathVariable String upc, Authentication auth) {
        System.out.println(auth);
        if (auth instanceof AuthToken) {
            CraveUser user = userRepo.findByUsernameIgnoreCase(auth.getName()).orElse(null);
            Item      item = repo.findByUpcUpc(upc).orElse(null);
            if (user == null)
                return ResponseObject.failure("error", "User not authenticated (Or not found).");
            else if (item == null)
                return ResponseObject.failure("error", "Item not found (" + upc + ")");
            else {
                user.getFavorites().remove(item);
                userRepo.save(user);
                return ResponseObject.success();
            }
        }

        return ResponseObject.failure();
    }

}
