package me.travja.crave.receiptservice.models;

import me.travja.crave.common.models.item.ProductInformation;

public class WalmartItem extends ProductInformation {

    public WalmartItem(String name, String upc, String image, String description, double price) {
        super(name, upc, image, description, price);
    }

    public WalmartItem() {}
}
