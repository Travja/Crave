package me.travja.crave.common.models.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInformation {

    public String name, upc, image, description;
    public double price;

}
