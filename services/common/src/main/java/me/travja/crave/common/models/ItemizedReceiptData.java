package me.travja.crave.common.models;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NoArgsConstructor;
import me.travja.crave.common.serialization.ItemizedDeserializer;
import org.springframework.util.MultiValueMap;

@NoArgsConstructor
@JsonDeserialize(using = ItemizedDeserializer.class)
public class ItemizedReceiptData extends SimpleReceiptData {

    public ItemizedReceiptData(ReceiptType type) {
        super(type);
    }

    public ItemizedReceiptData(MultiValueMap<String, String> map) {
        receiptType = SimpleReceiptData.ReceiptType.valueOf(map.getFirst("store-select"));

        int index = 0;
        while (map.containsKey("item-" + ++index + "-name")) {
            String name  = map.getFirst("item-" + index + "-name");
            String upc   = map.getFirst("item-" + index + "-upc");
            double price = Double.parseDouble(map.getFirst("item-" + index + "-price"));

            ProductInformation info = new ProductInformation(name, upc, price);
            getProductData().add(info);
        }
    }

}
