package me.travja.crave.common.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.exceptions.ItemNotFoundException;
import me.travja.crave.common.exceptions.StoreNotFoundException;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.item.Sale;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.ItemDetailsRepository;
import me.travja.crave.common.repositories.StoreRepository;
import me.travja.crave.common.util.Formatter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

public class SaleDeserializer extends StdDeserializer<Sale> {

    public SaleDeserializer() {
        this(null);
    }

    public SaleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Sale deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        ItemDetailsRepository itemRepo  = AppContext.getBean(ItemDetailsRepository.class);
        StoreRepository       storeRepo = AppContext.getBean(StoreRepository.class);

        JsonNode node = jp.getCodec().readTree(jp);
        Sale     sale = new Sale();

        long            storeId = node.get("storeId").asLong();
        Optional<Store> oStore  = storeRepo.findById(storeId);
        oStore.ifPresentOrElse(store -> sale.setStore(store),
                () -> {throw new StoreNotFoundException("Could not find store by id " + storeId);});

        String                upc   = node.get("item").asText();
        Optional<ItemDetails> oItem = itemRepo.findByItemUpcUpcAndStoreId(upc, storeId);
        oItem.ifPresentOrElse(item -> sale.setItem(item),
                () -> {throw new ItemNotFoundException("Could not find item by UPC " + upc);});


        double newPrice = node.get("newPrice").asDouble();
        sale.setNewPrice(newPrice);

        try {
            Date startDate = Formatter.parseDate(node.get("startDate").asText());
            Date endDate   = Formatter.parseDate(node.get("endDate").asText());
            sale.setStartDate(startDate);
            sale.setEndDate(endDate);
        } catch (ParseException e) {
            throw new IOException("Could not parse dates.", e);
        }

        return sale;
    }
}
