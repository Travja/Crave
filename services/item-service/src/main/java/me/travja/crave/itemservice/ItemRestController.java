package me.travja.crave.itemservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
public class ItemRestController {

    @GetMapping
    public String getItems() {
        return "Hello";
    }

}
