package me.travja.crave.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductInformation {

    public String name, upc;
    public double price;

}
