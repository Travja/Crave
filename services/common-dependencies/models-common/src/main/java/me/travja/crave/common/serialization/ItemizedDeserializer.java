package me.travja.crave.common.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.travja.crave.common.models.item.ItemizedReceiptData;
import me.travja.crave.common.models.item.ProductInformation;
import me.travja.crave.common.models.item.SimpleReceiptData;

import java.io.IOException;

public class ItemizedDeserializer extends StdDeserializer<ItemizedReceiptData> {

    public ItemizedDeserializer() {
        this(null);
    }

    public ItemizedDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ItemizedReceiptData deserialize(JsonParser jp, DeserializationContext context) throws IOException,
            JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ItemizedReceiptData data =
                new ItemizedReceiptData(SimpleReceiptData.ReceiptType.valueOf(node.get("store-select").asText()));

        int index = 0;

        while (node.has("item-" + ++index + "-name")) {
            System.out.println("Item " + index + " does exist.");
            String name  = node.get("item-" + index + "-name").asText();
            String upc   = node.get("item-" + index + "-upc").asText();
            double price = node.get("item-" + index + "-price").asDouble();

            ProductInformation info = new ProductInformation(name, upc, null, null, price);
            data.getProductData().add(info);
        }

        return data;
    }
}
