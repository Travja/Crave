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
import me.travja.crave.common.models.store.Location;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.ItemDetailsRepository;
import me.travja.crave.common.repositories.SaleRepository;
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
        SaleRepository        saleRepo  = AppContext.getBean(SaleRepository.class);

        JsonNode node = jp.getCodec().readTree(jp);

        if (node.has("id")) {
            Optional<Sale> sale = saleRepo.findById(node.get("id").asLong());
            return sale.orElse(null);
        }

        Sale sale = new Sale();
        long storeId;


        if (node.has("store")) {
            JsonNode storeNode = node.get("store");

            double lat = storeNode.get("lat").asDouble(),
                    lon = storeNode.get("lon").asDouble();
            String   storeName = storeNode.get("name").asText();
            String   address   = storeNode.get("address").asText();
            String[] split     = address.split(", ");
            String   streetAddress, city, state;

//            if (storeNode.get("name").asText().equalsIgnoreCase("Walmart")) {

            streetAddress = split[0];
            city = split[1];
            state = split[2].split(" ")[0];

            System.out.println("Street: " + streetAddress + " -- City: " + city + " -- State: " + state);

//            }

            Optional<Store> oStore = storeRepo.findStoreByStreetAddressAndCityAndState(streetAddress, city, state);

            Store store;
            if (oStore.isPresent()) {
                store = oStore.get();
            } else {
                store = new Store();
                store.setName(storeName);
                store.setStreetAddress(streetAddress);
                store.setCity(city);
                store.setState(state);
                store.setLocation(new Location(lat, lon));
                store = storeRepo.save(store);
            }
            storeId = store.getId();

        } else {
            JsonNode sNode = node.get("storeId");
            if (sNode == null) sNode = node.get("sid");
            storeId = sNode.asLong();
            Optional<Store> oStore       = storeRepo.findById(storeId);
            long            finalStoreId = storeId;
            oStore.ifPresentOrElse(store -> sale.setStore(store),
                    () -> {throw new StoreNotFoundException("Could not find store by id " + finalStoreId);});
        }

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
