package me.travja.crave.receiptservice;

import me.travja.crave.receiptservice.models.Item;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ReceiptRestController {

    private final ItemsRepository repo;

    public ReceiptRestController(ItemsRepository repo) {
        this.repo = repo;
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
