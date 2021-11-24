package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.ResponseObject;
import me.travja.crave.common.models.SortStrategy;
import me.travja.crave.common.models.auth.AuthToken;
import me.travja.crave.common.models.auth.CraveUser;
import me.travja.crave.common.models.item.*;
import me.travja.crave.common.models.store.Location;
import me.travja.crave.common.repositories.ItemService;
import me.travja.crave.common.repositories.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static me.travja.crave.common.views.CraveViews.DetailsView;
import static me.travja.crave.common.views.CraveViews.ItemView;

@RequiredArgsConstructor
@CraveController("/items")
public class ItemRestController {

    private final ItemService itemService;
    private final UserRepo    userRepo;

    @GetMapping
    @JsonView(ItemView.class)
    public Page<Item> getItems(@RequestParam(required = false) String query,
                               @RequestParam(required = false) String store,
                               @RequestParam(required = false, defaultValue = "0") Double distance,
                               @RequestParam(required = false, defaultValue = "0") Double lat,
                               @RequestParam(required = false, defaultValue = "0") Double lon,
                               @RequestParam(required = false, defaultValue = "ALPHABETICAL") SortStrategy sortStrategy,
                               @RequestParam(required = false, defaultValue = "0") int page,
                               @RequestParam(required = false, defaultValue = "50") int count,
                               Authentication auth) {
        Pageable pg = PageRequest.of(page, count,
                sortStrategy.getSort() //Get the sort strategy from the enum
                        .and(SortStrategy.ALPHABETICAL.getSort()));
        Page<Item> items;
        if (distance > 0)
            items = itemService.getAllFromStore(query, store, lat, lon, distance, sortStrategy, pg);
        else
            items = itemService.getAllFromStore(query, store, sortStrategy, pg);

        List<String> stores = new ArrayList<>();
        if (store != null && !store.isEmpty())
            stores = Arrays.stream(store.split(",")).map(str -> str.toLowerCase()).collect(Collectors.toList());

        Location location = null;
        if (lat != null && lon != null)
            location = new Location(lat, lon);

        if (!stores.isEmpty() || distance != null) {
            for (Item item : items) {
                Location     finalLocation = location;
                List<String> finalStores   = stores;
                Set<ItemDetails> details = item.getDetails().stream().filter(dets -> {
                    boolean good = true;
                    if (store != null && !finalStores.isEmpty() && !finalStores.contains(dets.getStore().getName().toLowerCase()))
                        good = false;

                    if (good && distance != null && distance > 0 && finalLocation != null)
                        good = dets.getStore().getDistance(finalLocation) <= distance;

                    return good;
                }).collect(Collectors.toSet());

                item.setDetails(details);
            }
        }

        if (auth != null) {
            Optional<CraveUser> user = userRepo.findByUsernameIgnoreCase(auth.getName());
            if (user.isPresent()) {
                items.stream()
                        .filter(itm -> user.get().getFavorites().contains(itm))
                        .forEach(itm -> itm.setFavorite(true));
            }
        }

        return items;
    }

    @GetMapping("/name/{upc}")
    public ResponseObject getName(@PathVariable String upc) {
        Item item = itemService.getItem(upc).orElse(null);
        if (item != null) {
            System.out.println(item.getName());
            return ResponseObject.success("name", item.getName());
        } else
            return ResponseObject.failure();
    }

    @GetMapping("/search")
    @JsonView(DetailsView.class)
    public Page<ListItem> searchNames(@RequestParam(required = false) String query,
                                      @RequestParam(required = false, defaultValue = "false") boolean detailed,
                                      @RequestParam(required = false, defaultValue = "-1") long storeId,
                                      @RequestParam(required = false, defaultValue = "ALPHABETICAL")
                                              SortStrategy sortStrategy,
                                      @RequestParam(required = false, defaultValue = "0") int page,
                                      @RequestParam(required = false, defaultValue = "4") int count) {
        if (query == null || query.isEmpty()) return Page.empty();

        Pageable   pg = PageRequest.of(page, count);
        Page<Item> items;
        if (storeId != -1)
            items = itemService.getAllFromStore(query, storeId, sortStrategy, pg);
        else
            items = itemService.getAllByName(query, pg);

        return new PageImpl<>(items.stream().map(item -> {
            if (detailed) {
                ItemDetails details = null;
                for (ItemDetails dets : item.getDetails()) {
                    if (details == null || details.getPrice() > dets.getPrice())
                        details = dets;

                    for (Sale sale : dets.getSales()) {
                        if (sale.getEndDate().before(new Date())) continue;

                        if (details == null || details.getPrice() > sale.getNewPrice()) {
                            details = dets;
                            details.setPrice(sale.getNewPrice());
                        }
                    }
                }
                return new DetailedListItem(item.getId(), item.getName(), false, details);
            } else
                return new ListItem(item.getId(), item.getName(), false);
        }).collect(Collectors.toList()), pg, items.getSize());
    }

    @GetMapping("/{upc}")
    @JsonView(ItemView.class)
    public Item getItem(@PathVariable String upc, Authentication auth) {
        Item item = itemService.getItem(upc).orElse(null);

        if (item != null && auth != null)
            userRepo.findByUsernameIgnoreCase(auth.getName())
                    .ifPresent(user -> item.setFavorite(user.getFavorites().contains(item)));

        return item;
    }

    @PostMapping
    @JsonView(ItemView.class)
    public Item createItem(@RequestBody RequestItem item) {
        return itemService.save(item.toItem());
    }

    @PatchMapping("/{upc}")
    @JsonView(ItemView.class)
    public Item patchOrCreateItem(@PathVariable String upc, @RequestBody RequestItem item, Authentication auth) {
        Optional<Item> it = itemService.getItem(upc);
        if (it.isPresent()) {
            List<String> authorities = auth != null
                    ? auth.getAuthorities().stream().map(at -> at.getAuthority()).collect(Collectors.toList())
                    : new ArrayList<>();

            //Update item
            Item itm = it.get();
            itm.update(new ProductInformation(itm.getName(), itm.getStringUpc(), itm.getImage(), itm.getDescription(), 0), authorities);
            return itemService.save(itm);
        } else {
            return itemService.save(item.toItem());
        }
    }

    @PostMapping("/favorite/{upc}")
    public ResponseObject favorite(@PathVariable String upc, Authentication auth) {
        System.out.println(auth);
        if (auth instanceof AuthToken) {
            CraveUser user = userRepo.findByUsernameIgnoreCase(auth.getName()).orElse(null);
            Item      item = itemService.getItem(upc).orElse(null);
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
            Item      item = itemService.getItem(upc).orElse(null);
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
