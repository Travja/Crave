package me.travja.crave.common.models.item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.travja.crave.common.models.store.Store;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

import static me.travja.crave.common.views.CraveViews.*;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@JsonView({ItemView.class, StoreView.class, DetailsView.class})
public class ItemDetails {

    @Id
    @GeneratedValue
    private long   id;
    @ManyToOne
    @JsonView(DetailsView.class)
    private Item   item;
    @ManyToOne
    @JsonView({ItemView.class, DetailsView.class})
    private Store  store;
    private double price;

    public ItemDetails(Item item, Store store, double price) {
        setItem(item);
        setStore(store);
        setPrice(price);
    }

    /**
     * Updates the item based on the given product information.
     * If the change results in a price change, the function
     * returns the price change.
     *
     * @param prodInfo {@link ProductInformation}
     * @return the change in price.
     */
    public double update(ProductInformation prodInfo) {
        double priceChange = 0;
        if (prodInfo.getPrice() > 0) {
            double prePrice  = price;
            double postPrice = prodInfo.getPrice();
            setPrice(prodInfo.getPrice());

            if (Math.abs(prePrice - postPrice) > 0.0001)
                priceChange = postPrice - prePrice;

        }
        item.update(prodInfo);
        return priceChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ItemDetails that = (ItemDetails) o;
        return Objects.equals(id, that.id);
    }
}
