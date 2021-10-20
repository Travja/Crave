package me.travja.crave.common.models;

import lombok.Data;

import javax.persistence.GeneratedValue;

@Data
public class RequestItem {

    @GeneratedValue
    private long id;

    private String name,
            description,
            upc,
            image;

    public Item toItem() {
        Item item = new Item(getId(), getName(), getDescription(), null, getImage());
        item.setUpc(upc);
        return item;
    }

}
