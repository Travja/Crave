package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.AsyncCaller;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.ItemService;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;

import javax.persistence.Entity;
import java.util.Map;

import static me.travja.crave.common.views.CraveViews.*;

@Slf4j
@Entity
@Getter
@Setter
@NoArgsConstructor
@Polymorphism(type = PolymorphismType.EXPLICIT)
@JsonView({ItemView.class, StoreView.class, DetailsView.class, SaleView.class})
public class PendingDetails extends Detail {
    public double originalPrice;

    public PendingDetails(Item item, Store store, double price) {
        super(item, store, price);
    }

    public ItemDetails toDetails() {
        ItemService itemService = AppContext.getBean(ItemService.class);
        ItemDetails details = itemService.getItemDetails(getItem().getId(), getStore().getId())
                .orElse(new ItemDetails(getItem(), getStore(), getPrice()));

//        details.setInStock(isInStock());
//        details.setCarried(isCarried());
//        details.setId(getId());
//        details.getSales().clear();
//        details.getSales().addAll(getSales());
        details.setPrice(getPrice());
        return details;
    }

    public double getOriginalPrice() {
        ItemService itemService = AppContext.getBean(ItemService.class);
        ItemDetails oDets       = itemService.getItemDetails(getItem().getStringUpc(), getStore().getId()).orElse(null);
        return oDets == null ? 0d : oDets.getPrice();
    }

    public void approve() {
        ItemService itemService = AppContext.getBean(ItemService.class);
        AsyncCaller async       = AppContext.getBean(AsyncCaller.class);
        double      prevPrice   = getOriginalPrice();
        ItemDetails dets        = toDetails();

        log.info("APPROVED: " + dets.toString());
        itemService.save(dets);
        itemService.delete(this);

        //We have a different price original price...
        // Check if we need to alert people.
        if (prevPrice != 0) {
            double priceChange = getPrice() - prevPrice;
            if (Math.abs(priceChange) > 0.0001) {
                async.handlePriceUpdates(Map.of(dets, priceChange));
            }
        }
    }

    public double calculatePercentChange() {
        double change = (getPrice() / getOriginalPrice()) - 1;
        change *= 100;
        return change;
    }
}
