package me.travja.crave.itemservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemRestController {

    @GetMapping
    public List<Item> getItems() {
        return Collections.singletonList(new Item("Test item"));
    }

}
