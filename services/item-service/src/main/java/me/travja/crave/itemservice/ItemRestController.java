package me.travja.crave.itemservice;

import me.travja.crave.common.Item;
import me.travja.crave.common.ItemsRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemRestController {

    private final ItemsRepository repo;

    public ItemRestController(ItemsRepository repo) {
        this.repo = repo;
        System.out.println("Rest Controller initialized!!!!! PAY ATTENTION TO ME!");
    }

    @GetMapping
    public List<Item> getItems() {
        return (List<Item>) repo.findAll();
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return repo.save(item);
    }

}
