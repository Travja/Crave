package me.travja.crave.receiptservice.models;

import lombok.Getter;
import lombok.Setter;
import me.travja.crave.receiptservice.TargetResponse;

public class TargetItem {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private double price;

    public TargetItem(TargetResponse.TargetData.Product product) {
        this.name = product.getItem().getProductDescription().getTitle();
        this.price = product.getPrice().getCurrentRetail();
    }

}
