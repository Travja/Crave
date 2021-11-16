package me.travja.crave.common.models.item;

import lombok.Data;
import me.travja.crave.common.models.item.Item;

import javax.persistence.GeneratedValue;
import java.util.ArrayList;

@Data
public class RequestItem {

    @GeneratedValue
    private long id;

    private String name,
            description,
            upc,
            image;

    public Item toItem() {
        Item item = new Item(getId(), getName(), getDescription(), null, getImage(), new ArrayList<>());
        item.setUpc(upc);
        return item;
    }

}