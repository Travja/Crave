package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.conf.AppContext;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.repositories.ItemService;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;

import javax.persistence.Entity;

import static me.travja.crave.common.views.CraveViews.*;

@Slf4j
@Entity
@NoArgsConstructor
@Polymorphism(type = PolymorphismType.EXPLICIT)
@JsonView({ItemView.class, StoreView.class, DetailsView.class, SaleView.class})
public class PendingDetails extends Detail {
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

    public void approve() {
        ItemService itemService = AppContext.getBean(ItemService.class);
        ItemDetails dets        = toDetails();
        log.info("APPROVED: " + dets.toString());
        itemService.save(dets);
        itemService.delete(this);
    }
}
