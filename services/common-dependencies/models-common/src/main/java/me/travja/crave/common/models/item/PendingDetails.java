package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.NoArgsConstructor;
import me.travja.crave.common.models.store.Store;

import javax.persistence.Entity;

import static me.travja.crave.common.views.CraveViews.*;

@Entity
@NoArgsConstructor
@JsonView({ItemView.class, StoreView.class, DetailsView.class, SaleView.class})
public class PendingDetails extends ItemDetails {
    public PendingDetails(Item item, Store store, double price) {
        super(item, store, price);
    }
}
