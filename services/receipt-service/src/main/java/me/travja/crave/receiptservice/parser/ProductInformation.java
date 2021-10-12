package me.travja.crave.receiptservice.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ProductInformation {

    @Getter
    @Setter
    public String name, upc;

    @Getter
    @Setter
    public double price;

    @Override
    public String toString() {
        return "ProductInformation{" +
                "name='" + name + '\'' +
                ", upc='" + upc + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
