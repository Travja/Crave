package me.travja.crave.common.models.item;


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
        map.keySet().forEach(key -> System.out.println(key + ": " + map.get(key)));

        receiptType = ReceiptType.valueOf(map.getFirst("store-select").toUpperCase().replace("'", "").split(" ")[0]);

        setStreetAddress(map.getFirst("address"));
        setCity(map.getFirst("city"));
        setState(map.getFirst("state"));

        int index = 0;
        while (map.containsKey("item-" + ++index + "-name")) {
            if (map.getFirst("item-" + index + "-name").isEmpty()) continue;

            try {
                String name  = map.getFirst("item-" + index + "-name");
                String upc   = map.getFirst("item-" + index + "-upc");
                double price = Double.parseDouble(map.getFirst("item-" + index + "-price"));

                ProductInformation info = new ProductInformation(name, new UPC(upc).getUPC(), null, null, price);
                getProductData().add(info);
            } catch (Exception e) {
                continue;
            }
        }
    }

}
